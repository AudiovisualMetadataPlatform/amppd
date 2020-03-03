package edu.indiana.dlib.amppd.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.Batch;
import edu.indiana.dlib.amppd.model.BatchFile;
import edu.indiana.dlib.amppd.model.BatchFile.SupplementType;
import edu.indiana.dlib.amppd.model.BatchSupplementFile;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.service.BatchService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.PreprocessService;
import edu.indiana.dlib.amppd.web.BatchValidationResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class BatchServiceImpl implements BatchService {
	@Autowired
	private AmppdPropertyConfig propertyConfig;
	
	@Autowired
	private ItemRepository itemRepository;
	@Autowired
	private CollectionRepository collectionRepository;
	@Autowired
	private PrimaryfileRepository primaryfileRepository;
	@Autowired
	private PrimaryfileSupplementRepository primaryfileSupplementRepository;
	@Autowired
	private CollectionSupplementRepository collectionSupplementRepository;
	@Autowired
	private ItemSupplementRepository itemSupplementRepository;
	@Autowired
	private FileStorageService fileStorageService;
	@Autowired
	private PreprocessService preprocessService;
	
	BatchValidationResponse batchValidationResponse;
	
	
	public List<String> processBatch(BatchValidationResponse batchValidation, String username) {
		List<String> errors = new ArrayList<String>();
		Batch batch = batchValidation.getBatch();
		for(BatchFile batchFile : batch.getBatchFiles()) {
			try {
				createItem(batch.getUnit(), batchFile, username);
				if(batchValidationResponse.hasProcessingErrors())
					errors.addAll(batchValidationResponse.getProcessingErrors());
			}
			catch(Exception ex) {
				log.error("Batch processing exception: " + ex.toString());
				errors.add("Error processing file #" + batchFile.getRowNum() + ". " + ex.toString());
			}
		}	
		return errors;
	}
	
	/*
	 * Create an item with the appropriate primary files, supplemental files, etc.
	 */
	private void createItem(Unit unit, BatchFile batchFile, String username) throws Exception {

		// Get the collection
		Collection collection = getUpdatedCollection(batchFile.getCollection().getId());
		collection.setUnit(unit);
		
		// Set the source directory based on dropbox, unit name and collection name
		String sourceDir = getSourceDir(unit, collection);

		// Get an existing item if found in this collection, otherwise create a new one. 
		Item item = getItem(collection, batchFile.getItemName(), batchFile.getItemDescription(), username, batchFile.getExternalItemId(), batchFile.getExternalSource());
		if(batchValidationResponse.hasProcessingErrors())
			return;
		
		collection.addItem(item);
		
		// Process the files based on the supplement type
		if(batchFile.getSupplementType()==SupplementType.PRIMARYFILE || batchFile.getSupplementType()==null) {
			// Either get an existing or create a primary file
			Primaryfile primaryFile = createPrimaryfile(collection, item, batchFile, username, sourceDir);
	    	
			if(batchFile.getSupplementType()==SupplementType.PRIMARYFILE) {
				// For each primary file supplememnt, create the object and then move the file to it's destination
				for(BatchSupplementFile batchSupplementFile : batchFile.getBatchSupplementFiles()) {
					createPrimaryfileSupplement(primaryFile, batchSupplementFile, username, sourceDir);
				}
			}
		}
		else if(batchFile.getSupplementType()==SupplementType.COLLECTION) {
			for(BatchSupplementFile batchSupplementFile : batchFile.getBatchSupplementFiles()) {
				createCollectionSupplement(collection, batchSupplementFile, username, sourceDir);
			}
		}
		else if(batchFile.getSupplementType()==SupplementType.ITEM) {
			for(BatchSupplementFile batchSupplementFile : batchFile.getBatchSupplementFiles()) {
				createItemSupplement(item, batchSupplementFile, username, sourceDir);
			}
		}
				
	}
	
	/*
	 * Create a primary file, store it in the database, move it to it's destination in amppd storage, log that the file was created
	 */
	private Primaryfile createPrimaryfile(Collection batchfileCollection, Item item, BatchFile batchFile, String username, String sourceDir) throws Exception {
		
		try {
			// Either get an existing or create a primary file
			Primaryfile primaryFile = getPrimaryfile(batchfileCollection, item, batchFile, username);
			primaryFile.setItem(item);
			primaryfileRepository.save(primaryFile);
			
			// Move the file from the dropbox to amppd file storage
			String targetDir = fileStorageService.getDirPathname(item);
			Path targetPath = moveFile(sourceDir, targetDir, batchFile.getPrimaryfileFilename(), fileStorageService.getFilePathname(primaryFile));
	    	primaryFile.setPathname(fileStorageService.getFilePathname(primaryFile));
	
	    	logFileCreated(primaryFile, targetPath);	    	
			
			// preprocess the supplement after ingest
			preprocessService.preprocess(primaryFile);
	    	return primaryFile;
		}
		catch(IOException ex) {
			
			throw new Exception(String.format("Error creating primary file %s.  Error is: %s", batchFile.getPrimaryfileFilename(), ex.toString()));
		}
	}
	
	/*
	 * Create a primary file supplement, store it in the database, move it to it's destination in amppd storage, log that the file was created
	 */
	private void createPrimaryfileSupplement(Primaryfile primaryFile, BatchSupplementFile batchSupplementFile, String username, String sourceDir) throws Exception {
		try {
			String targetDir = fileStorageService.getDirPathname(primaryFile);
			PrimaryfileSupplement supplement = createPrimaryfileSupplement(primaryFile, batchSupplementFile, username);
			primaryfileSupplementRepository.save(supplement);
			
			// Move the file from the dropbox to amppd file storage
			Path targetSuppPath = moveFile(sourceDir, targetDir, batchSupplementFile.getSupplementFilename(), fileStorageService.getFilePathname(supplement));
			supplement.setPathname(fileStorageService.getFilePathname(supplement));
			
			// Log that the file was created
	    	logFileCreated(supplement, targetSuppPath);
			// preprocess the supplement after ingest
			//preprocessService.preprocess(supplement);
		}
		catch(IOException ex) {
			throw new Exception(String.format("Error creating primary file supplement %s.  Error is: %s", batchSupplementFile.getSupplementFilename(), ex.toString()));
		}
	}

	/*
	 * Create a collection file supplement, store it in the database, move it to it's destination in amppd storage, log that the file was created
	 */
	private void createCollectionSupplement(Collection collection, BatchSupplementFile batchSupplementFile, String username, String sourceDir) throws Exception {
		try {
			// For collection supplements, create supplements and then move the files to their destination
			String targetDir = fileStorageService.getDirPathname(collection);
			
			CollectionSupplement supplement = getCollectionSupplement(collection, batchSupplementFile, username);
			collectionSupplementRepository.save(supplement);
			
			// Move the file from the dropbox to amppd file storage
			Path targetSuppPath = moveFile(sourceDir, targetDir, batchSupplementFile.getSupplementFilename(), fileStorageService.getFilePathname(supplement));
			supplement.setPathname(fileStorageService.getFilePathname(supplement));
			// Log that the file was created
			logFileCreated(supplement, targetSuppPath);
			// preprocess the supplement after ingest
			//preprocessService.preprocess(supplement);
		}
		catch(IOException ex) {
			throw new Exception(String.format("Error creating collection supplement %s.  Error is: %s", batchSupplementFile.getSupplementFilename(), ex.toString()));
		}
	}
	
	/*
	 * Create a item file supplement, store it in the database, move it to it's destination in amppd storage, log that the file was created
	 */
	private void createItemSupplement(Item item, BatchSupplementFile batchSupplementFile, String username, String sourceDir) throws Exception {
		try {
			// For item supplements, create supplements and then move the files to their destination
			String targetDir = fileStorageService.getDirPathname(item);
			
			ItemSupplement supplement = getItemSupplement(item, batchSupplementFile, username);
			itemSupplementRepository.save(supplement);
			
			// Move the file from the dropbox to amppd file storage
			Path targetSuppPath = moveFile(sourceDir, targetDir, batchSupplementFile.getSupplementFilename(), fileStorageService.getFilePathname(supplement));
			supplement.setPathname(fileStorageService.getFilePathname(supplement));
			// Log that the file was created
			logFileCreated(supplement, targetSuppPath);
			// preprocess the supplement after ingest
			//preprocessService.preprocess(supplement);
		}
		catch(IOException ex) {
			throw new Exception(String.format("Error creating item supplement %s.  Error is: %s", batchSupplementFile.getSupplementFilename(), ex.toString()));
		}
	}
	
	/*
	 * Create an item supplememt if it does not already exists
	 */
	private ItemSupplement getItemSupplement(Item item, BatchSupplementFile batchSupplementFile, String username){
		//check if the supplement exists
		ItemSupplement itemSupplement = null;
		if(item.getSupplements() != null)
		{
			for(ItemSupplement is : item.getSupplements()) 
			{
				if(is.getName()==batchSupplementFile.getSupplementName())
				{
					//report an error to avoid reupload 
				}
			}
		}
				
		// Create Supplement if doesn't already exists
		if(itemSupplement == null) 
		{
			itemSupplement = new ItemSupplement();
			itemSupplement.setItem(item);
			itemSupplement.setName(batchSupplementFile.getSupplementName());
			itemSupplement.setOriginalFilename(batchSupplementFile.getSupplementFilename());
			itemSupplement.setDescription(batchSupplementFile.getSupplementDescription());
			itemSupplement.setCreatedBy(username);
			itemSupplement.setModifiedBy(username);
			itemSupplement.setCreatedDate(new Date().getTime());
			itemSupplement.setModifiedDate(new Date().getTime());
		}
		return itemSupplement;
	}
	
	/*
	 * Create a collection supplement if it does not already exists
	 */
	private CollectionSupplement getCollectionSupplement(Collection collection, BatchSupplementFile batchSupplementFile, String username){
		//check if the supplement exists
		CollectionSupplement collectionSupplement = null;
		if(collection.getSupplements() != null)
		{
			for(CollectionSupplement cs : collection.getSupplements()) 
			{
				if(cs.getName()==batchSupplementFile.getSupplementName())
				{
					//report an error to avoid reupload 
				}
			}
		}
				
		// Create Supplement if doesn't already exists
		if(collectionSupplement == null) 
		{
			collectionSupplement = new CollectionSupplement();
			collectionSupplement.setCollection(collection);
			collectionSupplement.setName(batchSupplementFile.getSupplementName());
			collectionSupplement.setOriginalFilename(batchSupplementFile.getSupplementFilename());
			collectionSupplement.setDescription(batchSupplementFile.getSupplementDescription());
			collectionSupplement.setCreatedBy(username);
			collectionSupplement.setModifiedBy(username);
			collectionSupplement.setCreatedDate(new Date().getTime());
			collectionSupplement.setModifiedDate(new Date().getTime());
		}
		
		return collectionSupplement;
	}
	
	/*
	 * Create a primary file supplement
	 */
	private PrimaryfileSupplement createPrimaryfileSupplement(Primaryfile primaryFile, BatchSupplementFile batchSupplementFile, String username){
		//check if the supplement exists
		PrimaryfileSupplement primaryfileSupplement = null;
		if(primaryFile.getSupplements() != null)
		{
			for(PrimaryfileSupplement ps : primaryFile.getSupplements()) 
			{
				if(ps.getName()==batchSupplementFile.getSupplementFilename())
				{
					//report an error to avoid reupload 
				}
			}
		}
		
		// Create Supplements
		if(primaryfileSupplement == null) 
		{
			primaryfileSupplement = new PrimaryfileSupplement();
			primaryfileSupplement.setPrimaryfile(primaryFile);
			primaryfileSupplement.setName(batchSupplementFile.getSupplementName());
			primaryfileSupplement.setOriginalFilename(batchSupplementFile.getSupplementFilename());
			primaryfileSupplement.setDescription(batchSupplementFile.getSupplementDescription());
			primaryfileSupplement.setCreatedBy(username);
			primaryfileSupplement.setModifiedBy(username);
			primaryfileSupplement.setCreatedDate(new Date().getTime());
			primaryfileSupplement.setModifiedDate(new Date().getTime());
		}
		return primaryfileSupplement;
	}
	
	/*
	 * Either create a primary file or get an existing one 
	 */
	private Primaryfile getPrimaryfile(Collection batchfileCollection, Item item, BatchFile batchFile, String username) {
		Primaryfile primaryFile =null;
		
		// Check to see if the primary file exists
		if(item.getPrimaryfiles()!=null) {
			for(Primaryfile p : item.getPrimaryfiles()) {
				Collection primaryFileCollection = item.getCollection();
				//if(batchfileCollection.getName()==primaryFileCollection.getName() && item.getExternalIds().equals(batchFile.getExternalItemId()) && p.getName()==batchFile.getPrimaryfileName()) {
				if(p.getName()==batchFile.getPrimaryfileName())
				{
					//issue an error that primary file already exists
					//primaryFile = p;
					break;
				}
			}
		}
		
		// If it doesn't exist, create a new one
		if(primaryFile==null) {
			// Create Primary files		
			primaryFile = new Primaryfile();
			primaryFile.setName(batchFile.getPrimaryfileName());
			primaryFile.setDescription(batchFile.getPrimaryfileDescription());
			primaryFile.setOriginalFilename(batchFile.getPrimaryfileFilename());
			primaryFile.setCreatedBy(username);
			primaryFile.setCreatedDate(new Date().getTime());
		}
		
		primaryFile.setModifiedBy(username);
		primaryFile.setModifiedDate(new Date().getTime());
		primaryFile.setItem(item);
		
		return primaryFile;
					
	}
	
	/*
	 * Gets an Item object.  If one already exists, 
	 */
	private Item getItem(Collection collection, String itemName, String itemDescription, String createdBy, String externalItemId, String externalSource) {
		Item item = null;
		Set<Item> items = collection.getItems();
		boolean found = false;
		if(items!=null) {
			for(Item i : items) {
				if(!externalSource.isBlank() && !externalItemId.isBlank()) 
				{
					if(i.getExternalIds().containsKey(externalItemId))
					{
						if(i.getName().contentEquals(itemName))
						{
							//throw an error for duplication
							batchValidationResponse.addError("ERROR: Item external ID and Item name combination already exists");
							return item;
						}
						else {
							found = true;
							item = i;
							itemRepository.updateTitle(itemName, i.getId());
						}
					}
				}
				else if(i.getName().contentEquals(itemName)) {
					//throw an error for duplication
					batchValidationResponse.addError("ERROR: Item name already exists");
					return item;
				}
				if(found)
					break;
			}
		}
		// If item doesn't already exists
		if(item==null && !found) {
			item = new Item();
			item.setName(itemName);
			if(!externalSource.isBlank() && !externalItemId.isBlank()) {
				HashMap<String, String> externalIds = new HashMap<String, String>();
				externalIds.put(externalItemId, externalSource);
				item.setExternalIds(externalIds);
			}
			item.setDescription(itemDescription);
			item.setCreatedBy(createdBy);
			item.setModifiedBy(createdBy);
			item.setCreatedDate(new Date().getTime());
			item.setModifiedDate(new Date().getTime());
			item.setCollection(collection);
			itemRepository.save(item);
		}
		
		return item;
	}
	
	/*
	 * Move the file using hard links
	 */
	private Path moveFile(String sourceDir, String targetDir, String sourceFilename, String targetFilename) throws IOException {
		
		// Check to see if the folder exists on the file system.  If not, create it.
		if(!Files.exists(Paths.get(propertyConfig.getFileStorageRoot(), targetDir)))
		{
			Files.createDirectories(Paths.get(propertyConfig.getFileStorageRoot(), targetDir));
		}
		
		// Create paths from /{root}/{unit}/{collection}/{filename}
		Path existingFile = Paths.get(propertyConfig.getDropboxRoot(), sourceDir, sourceFilename);	
		Path newLink = Paths.get(propertyConfig.getFileStorageRoot(), targetFilename);	
		
		// Move the file
		fileStorageService.moveFile(existingFile, newLink);
		
		return newLink;
	}
	
	/*
	 * Methods for logging when a file was created
	 */
	private void logFileCreated(Primaryfile primaryFile, Path targetPath) {
    	log.info(String.format("Primaryfile %s has media file %s successfully uploaded to %s.", primaryFile.getId(), primaryFile.getOriginalFilename(), targetPath));
	}
	
	private void logFileCreated(PrimaryfileSupplement supplement, Path targetPath) {
    	log.info(String.format("Primary file supplement %s has media file %s successfully uploaded to %s.", supplement.getId(), supplement.getOriginalFilename(), targetPath));
	}
	
	private void logFileCreated(ItemSupplement supplement, Path targetPath) {
    	log.info(String.format("Item supplement %s has media file %s successfully uploaded to %s.", supplement.getId(), supplement.getOriginalFilename(), targetPath));
	}
	
	private void logFileCreated(CollectionSupplement supplement, Path targetPath) {
    	log.info(String.format("Collection supplement %s has media file %s successfully uploaded to %s.", supplement.getId(), supplement.getOriginalFilename(), targetPath));
	}
	
	private Collection getUpdatedCollection(Long collectionId) {
		return collectionRepository.findById(collectionId).get();
	}

	private String getSourceDir(Unit unit, Collection collection) {
		
		return String.format("/%s/%s", unit.getName(), collection.getName());
	}
	
}
