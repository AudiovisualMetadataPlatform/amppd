package edu.indiana.dlib.amppd.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
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
import edu.indiana.dlib.amppd.web.BatchValidationResponse;

@Service
@Transactional
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
	
	
	public boolean processBatch(BatchValidationResponse batchValidation, String username) {
		try {
			
			Batch batch = batchValidation.getBatch();
			
			for(BatchFile batchFile : batch.getBatchFiles()) {
				createItem(batch.getUnit(), batchFile, username);
			}	
			
			return true;
		}
		catch(Exception ex) {
			return false;
		}
	}
	
	private String getSourceDir(Unit unit, Collection collection) {
		
		return String.format("/%s/%s", unit.getName(), collection.getName());
	}
	
	/*
	 * Create an item with the appropriate primary files, supplemental files, etc.
	 */
	private void createItem(Unit unit, BatchFile batchFile, String username) throws IOException {

		// Get the collection
		Collection collection = getUpdatedCollection(batchFile.getCollection().getId());
		collection.setUnit(unit);
		
		// Set the source directory based on dropbox, unit name and collection name
		String sourceDir = getSourceDir(unit, collection);

		// Get an existing item if found in this collection, otherwise create a new one. 
		Item item = getItem(collection, batchFile.getItemName(), batchFile.getItemDescription(), username);
		
		collection.addItem(item);
		
		// Process the files based on the supplement type
		if(batchFile.getSupplementType()==SupplementType.PRIMARYFILE || batchFile.getSupplementType()==null) {
			
			// Either get an existing or create a primary file
			Primaryfile primaryFile = getPrimaryfile(item, batchFile, username);
			primaryFile.setItem(item);
			primaryfileRepository.save(primaryFile);
			
			// Move the file from the dropbox to amppd file storage
			String targetDir = fileStorageService.getDirPathname(item);
			moveFile(sourceDir, targetDir, batchFile.getPrimaryfileFilename(), fileStorageService.getFilePathname(primaryFile));
			
			if(batchFile.getSupplementType()==SupplementType.PRIMARYFILE) {
				targetDir = fileStorageService.getDirPathname(primaryFile);
				// For each primary file supplememnt, create the object and then move the file to it's destination
				for(BatchSupplementFile batchSupplementFile : batchFile.getBatchSupplementFiles()) {
					PrimaryfileSupplement supplement = createPrimaryfileSupplement(primaryFile, batchSupplementFile, username);
					primaryfileSupplementRepository.save(supplement);
					// Move the file from the dropbox to amppd file storage
					moveFile(sourceDir, targetDir, batchSupplementFile.getSupplementFilename(), fileStorageService.getFilePathname(supplement));
				}
			}

		}
		else if(batchFile.getSupplementType()==SupplementType.COLLECTION) {
			// For collection supplements, create supplements and then move the files to their destination
			String targetDir = fileStorageService.getDirPathname(collection);
			for(BatchSupplementFile batchSupplementFile : batchFile.getBatchSupplementFiles()) {
				CollectionSupplement supplement = createCollectionSupplement(collection, batchSupplementFile, username);
				collectionSupplementRepository.save(supplement);
				// Move the file from the dropbox to amppd file storage
				moveFile(sourceDir, targetDir, batchSupplementFile.getSupplementFilename(), fileStorageService.getFilePathname(supplement));
			}
			
		}
		else if(batchFile.getSupplementType()==SupplementType.ITEM) {
			// For item supplements, create supplements and then move the files to their destination
			String targetDir = fileStorageService.getDirPathname(item);
			for(BatchSupplementFile batchSupplementFile : batchFile.getBatchSupplementFiles()) {
				ItemSupplement supplement = createItemSupplement(item, batchSupplementFile, username);
				itemSupplementRepository.save(supplement);
				// Move the file from the dropbox to amppd file storage
				moveFile(sourceDir, targetDir, batchSupplementFile.getSupplementFilename(), fileStorageService.getFilePathname(supplement));
			}
		}
				
	}
	private Collection getUpdatedCollection(Long collectionId) {
		return collectionRepository.findById(collectionId).get();
	}
	/*
	 * Create an item supplememt
	 */
	private ItemSupplement createItemSupplement(Item item, BatchSupplementFile batchSupplementFile, String username){
		// Create Supplement
		ItemSupplement itemSupplement = new ItemSupplement();
		itemSupplement.setItem(item);
		itemSupplement.setName(batchSupplementFile.getSupplementName());
		itemSupplement.setOriginalFilename(batchSupplementFile.getSupplementFilename());
		itemSupplement.setDescription(batchSupplementFile.getSupplementDescription());
		itemSupplement.setCreatedBy(username);
		itemSupplement.setModifiedBy(username);
		itemSupplement.setCreatedDate(new Date().getTime());
		itemSupplement.setModifiedDate(new Date().getTime());
		return itemSupplement;
	}
	
	/*
	 * Create a collection supplement
	 */
	private CollectionSupplement createCollectionSupplement(Collection collection, BatchSupplementFile batchSupplementFile, String username){
		
		CollectionSupplement collectionSupplement = new CollectionSupplement();
		collectionSupplement.setCollection(collection);
		collectionSupplement.setName(batchSupplementFile.getSupplementName());
		collectionSupplement.setOriginalFilename(batchSupplementFile.getSupplementFilename());
		collectionSupplement.setDescription(batchSupplementFile.getSupplementDescription());
		collectionSupplement.setCreatedBy(username);
		collectionSupplement.setModifiedBy(username);
		collectionSupplement.setCreatedDate(new Date().getTime());
		collectionSupplement.setModifiedDate(new Date().getTime());
		
		return collectionSupplement;
	}
	
	/*
	 * Create a primary file supplement
	 */
	private PrimaryfileSupplement createPrimaryfileSupplement(Primaryfile primaryFile, BatchSupplementFile batchSupplementFile, String username){
		
		// Create Supplements
		PrimaryfileSupplement primaryfileSupplement = new PrimaryfileSupplement();
		primaryfileSupplement.setPrimaryfile(primaryFile);
		primaryfileSupplement.setName(batchSupplementFile.getSupplementName());
		primaryfileSupplement.setOriginalFilename(batchSupplementFile.getSupplementFilename());
		primaryfileSupplement.setDescription(batchSupplementFile.getSupplementDescription());
		primaryfileSupplement.setCreatedBy(username);
		primaryfileSupplement.setModifiedBy(username);
		primaryfileSupplement.setCreatedDate(new Date().getTime());
		primaryfileSupplement.setModifiedDate(new Date().getTime());
		return primaryfileSupplement;
	}
	
	/*
	 * Either create a primary file or get an existing one 
	 */
	private Primaryfile getPrimaryfile(Item item, BatchFile batchFile, String username) {
		Primaryfile primaryFile =null;
		
		// Check to see if the primary file exists
		if(item.getPrimaryfiles()!=null) {
			for(Primaryfile p : item.getPrimaryfiles()) {
				if(p.getName()==batchFile.getPrimaryfileName()) {
					primaryFile = p;
					break;
				}
			}
		}
		
		// If it doesn't exist, create a new one
		if(primaryFile==null) {
			// Create Primary files		
			primaryFile = new Primaryfile();
			primaryFile.setName(batchFile.getPrimaryfileName());
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
	private Item getItem(Collection collection, String itemName, String itemDescription, String createdBy) {
		Item item = null;
		
		Set<Item> items = collection.getItems();
		if(items!=null) {
			for(Item i : items) {
				if(i.getName().contentEquals(itemName)) {
					item = i;
					break;
				}
			}
		}
		// Check if it already exists
		if(item==null) {
			// if not, create it
			item = new Item();
			item.setName(itemName);
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
	private void moveFile(String sourceDir, String targetDir, String sourceFilename, String targetFilename) throws IOException {
		
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
		
	}
	
	
}
