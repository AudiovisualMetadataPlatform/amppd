package edu.indiana.dlib.amppd.service.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.Batch;
import edu.indiana.dlib.amppd.model.BatchFile;
import edu.indiana.dlib.amppd.model.BatchSupplementFile;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.service.BatchService;
import edu.indiana.dlib.amppd.service.DropboxService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.PreprocessService;
import edu.indiana.dlib.amppd.web.BatchResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of BatchService.
 */ 
@Service
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
	private DropboxService dropboxService;
	@Autowired
	private FileStorageService fileStorageService;
	@Autowired
	private PreprocessService preprocessService;
	
	int currRow;
	
	@Transactional
	public BatchResponse processBatch(BatchResponse response, String username) {
		List<String> errors = new ArrayList<String>();
		Batch batch = response.getBatch();

		for (BatchFile batchFile : batch.getBatchFiles()) {
			errors = new ArrayList<String>();
			currRow = batchFile.getRowNum();
			try {
				createItem(batch.getUnit(), batchFile, username, errors);
				if (errors.size()>0) {
					response.addProcessingErrors(errors);
				}
			}
			catch(Exception ex) {
				log.error("Batch processing exception on line #" + batchFile.getRowNum(), ex);
				response.addProcessingError("Error processing batch on line #" + batchFile.getRowNum() + ": " + ex);
			}
		}	
		
		boolean success = !response.hasProcessingErrors();
		response.setSuccess(success);
		if (success) {
			log.info("Successfully processed batch ingest " + response.getBatch());
		}
		else {
			log.error("Failed to process batch ingest " + response.getBatch());
			for (String error : response.getProcessingErrors()) {
				log.error(error);
			}
		}
		
		return response;
	}
	
	/*
	 * Create an item with the appropriate primaryfiles, supplemental files, etc.
	 */
	private void createItem(Unit unit, BatchFile batchFile, String username, List<String> errors) throws Exception {
		// Get the collection
		log.debug("BATCH PROCESSING : getting the updated collection");
		Collection collection = getUpdatedCollection(batchFile.getCollection().getId());
		collection.setUnit(unit);
		
		// Set the source directory based on dropbox, unit name and collection name
		log.debug("BATCH PROCESSING : Set the source directory based on dropbox, unit name and collection name");
		String sourceDir = getSourceDir(unit, collection);

		// Get an existing item if found in this collection, otherwise create a new one. 
		log.debug("BATCH PROCESSING : Get an existing item if found in this collection, otherwise create a new one");
		Item item = getItem(collection, batchFile.getItemName(), batchFile.getItemDescription(), username, batchFile.getExternalId(), batchFile.getExternalSource());
		if (item == null) throw new Exception("item with same name already existed");
		collection.addItem(item);
		log.debug("BATCH PROCESSING : The item found was added to the collection");
		
		// Process the files based on the supplement type
		if(batchFile.getSupplementType()==SupplementType.PRIMARYFILE || batchFile.getSupplementType()==null) {
			// Either get an existing or create a primaryfile
			log.debug("BATCH PROCESSING : Get an existing primaryfile otherwise create a new one");
			Primaryfile primaryfile = createPrimaryfile(collection, item, batchFile, username, sourceDir, errors);
			if (errors.size()>0)
				return;
	    	
			if(batchFile.getSupplementType()==SupplementType.PRIMARYFILE) {
				// For each primaryfile supplememnt, create the object and then move the file to it's destination
				log.debug("BATCH PROCESSING : For each primaryfile supplememnt, create the object and then move the file to it's destination");
				for(BatchSupplementFile batchSupplementFile : batchFile.getBatchSupplementFiles()) {
					createPrimaryfileSupplement(primaryfile, batchSupplementFile, username, sourceDir, errors);
					if(errors.size()>0 )
						break;
				}
			}
		}
		else if(batchFile.getSupplementType()==SupplementType.COLLECTION && errors.size()==0 ) {
			log.debug("BATCH PROCESSING : For each collection supplememnt, create the object and then move the file to it's destination");
			for(BatchSupplementFile batchSupplementFile : batchFile.getBatchSupplementFiles()) {
				createCollectionSupplement(collection, batchSupplementFile, username, sourceDir, errors);
			}
		}
		else if(batchFile.getSupplementType()==SupplementType.ITEM && errors.size()==0 ) {
			log.debug("BATCH PROCESSING : For each item supplememnt, create the object and then move the file to it's destination");
			for(BatchSupplementFile batchSupplementFile : batchFile.getBatchSupplementFiles()) {
				createItemSupplement(item, batchSupplementFile, username, sourceDir, errors);
			}
		}				
	}
	
	/*
	 * Create a primaryfile, store it in the database, move it to it's destination in amppd storage, log that the file was created
	 */
	private Primaryfile createPrimaryfile(Collection batchfileCollection, Item item, BatchFile batchFile, String username, String sourceDir, List<String> errors) throws Exception {		
		try {			
			Primaryfile primaryfile = getPrimaryfile(batchfileCollection, item, batchFile, username, errors);
			
			// ingest a new primaryfile only if not previously existing, and return the saved primaryfile; otherwise, 
			// this batchFile is for ingesting PrimaryfileSupplement only, in which case the existing primaryfile is returned
			if(errors.size()==0 && primaryfile.getId() == null) {
				primaryfile.setItem(item);
				Set<Primaryfile> primaryfilesSet = item.getPrimaryfiles();
				if(primaryfilesSet == null) {
					primaryfilesSet = new HashSet<Primaryfile>();
				}
				primaryfilesSet.add(primaryfile);
				item.setPrimaryfiles(primaryfilesSet);
				
				// set primaryfile pathname to the absolute pathname of the file to be ingested,
				// so that pre-process can resolve the path correctly without using media's root dir 
				Path existingFile = Paths.get(sourceDir, batchFile.getPrimaryfileFilename());	
				primaryfile.setPathname(existingFile.toString());
				
				// preprocess (and save if success) the primaryfile before moving files, i.e. while it's still in dropbox, 
				// so that in case preprocess fails, no primaryfile will be created with empty media info
				preprocessService.preprocess(primaryfile, true);
				
				log.debug("BATCH PROCESSING : Move the primaryfile " +  primaryfile.getOriginalFilename() + " from the dropbox to amppd media storage");
		    	primaryfile.setPathname(fileStorageService.getFilePathname(primaryfile));		    	
				
				// Move the file from the dropbox to amppd file storage:
				// need to use originalFilename instead of batchFile.getPrimaryfileFilename() for source filename,
				// as the latter might have been converted from flac to wav during preprocess,
				// while the former would have been updated to the generated wav file in this case
				Path targetPath = moveFile(sourceDir, primaryfile.getOriginalFilename(), primaryfile.getPathname());
				
				// save primaryfile after files are moved
		    	primaryfile = primaryfileRepository.save(primaryfile);
		    	
		    	logFileCreated(primaryfile, targetPath);	    					
			}
			
	    	return primaryfile;
		}
		catch(Exception ex) {			
			throw new RuntimeException("Error creating primaryfile " + batchFile.getPrimaryfileFilename(), ex);
		}
	}
	
	/*
	 * Create a primaryfile supplement, store it in the database, move it to it's destination in amppd storage, log that the file was created
	 */
	private void createPrimaryfileSupplement(Primaryfile primaryfile, BatchSupplementFile batchSupplementFile, String username, String sourceDir, List<String> errors) throws Exception {
		try {
			PrimaryfileSupplement supplement = createPrimaryfileSupplement(primaryfile, batchSupplementFile, username, errors);
			
			if(errors.size()==0 && supplement != null) {
				// set supplement pathname to the absolute pathname of the file to be ingested,
				// so that pre-process can resolve the path correctly without using media's root dir 
				Path existingFile = Paths.get(sourceDir, batchSupplementFile.getSupplementFilename());	
				supplement.setPathname(existingFile.toString());
				
				// preprocess (and save if success) the supplement before moving files, i.e. while it's still in dropbox
				// so that in case preprocess fails, no supplement will be created with empty media info
				preprocessService.preprocess(supplement, true);
				
				// Move the file from the dropbox to amppd file storage:
				// need to use originalFilename instead of batchFile.getPrimaryfileFilename() for source filename,
				// as the latter might have been converted from flac to wav during preprocess,
				// while the former would have been updated to the generated wav file in this case
				supplement.setPathname(fileStorageService.getFilePathname(supplement));				
				Path targetSuppPath = moveFile(sourceDir, supplement.getOriginalFilename(), supplement.getPathname());

				// save supplement after files are moved
				primaryfileSupplementRepository.save(supplement);
				
				// Log that the file was created
		    	logFileCreated(supplement, targetSuppPath);		    	
			}
		}
		catch(IOException ex) {
			throw new Exception(String.format("Error creating primaryfile supplement %s: %s", batchSupplementFile.getSupplementFilename(), ex.toString()), ex);
		}
	}

	/*
	 * Create a collection file supplement, store it in the database, move it to it's destination in amppd storage, log that the file was created
	 */
	private void createCollectionSupplement(Collection collection, BatchSupplementFile batchSupplementFile, String username, String sourceDir, List<String> errors) throws Exception {
		try {
			// For collection supplements, create supplements and then move the files to their destination	
			CollectionSupplement supplement = getCollectionSupplement(collection, batchSupplementFile, username, errors);
			
			if(supplement != null && errors.size()==0) {
				// set supplement pathname to the absolute pathname of the file to be ingested,
				// so that pre-process can resolve the path correctly without using media's root dir 
				Path existingFile = Paths.get(sourceDir, batchSupplementFile.getSupplementFilename());	
				supplement.setPathname(existingFile.toString());
				
				// preprocess (and save if success) the supplement before moving files, i.e. while it's still in dropbox
				// so that in case preprocess fails, no supplement will be created with empty media info
				preprocessService.preprocess(supplement, true);
				
				// Move the file from the dropbox to amppd file storage:
				// need to use originalFilename instead of batchFile.getPrimaryfileFilename() for source filename,
				// as the latter might have been converted from flac to wav during preprocess,
				// while the former would have been updated to the generated wav file in this case
				supplement.setPathname(fileStorageService.getFilePathname(supplement));				
				Path targetSuppPath = moveFile(sourceDir, supplement.getOriginalFilename(), supplement.getPathname());

				// save supplement after files are moved
				collectionSupplementRepository.save(supplement);
				
				// Log that the file was created
				logFileCreated(supplement, targetSuppPath);
			}
		}
		catch(IOException ex) {
			throw new Exception(String.format("Error creating collection supplement %s: %s", batchSupplementFile.getSupplementFilename(), ex.toString()), ex);
		}
	}
	
	/*
	 * Create a item file supplement, store it in the database, move it to it's destination in amppd storage, log that the file was created
	 */
	private void createItemSupplement(Item item, BatchSupplementFile batchSupplementFile, String username, String sourceDir, List<String> errors) throws Exception {
		try {
			ItemSupplement supplement = getItemSupplement(item, batchSupplementFile, username, errors);
			
			if(supplement != null && errors.size()==0) {
				// set supplement pathname to the absolute pathname of the file to be ingested,
				// so that pre-process can resolve the path correctly without using media's root dir 
				Path existingFile = Paths.get(sourceDir, batchSupplementFile.getSupplementFilename());	
				supplement.setPathname(existingFile.toString());
				
				// preprocess (and save if success) the supplement before moving files, i.e. while it's still in dropbox
				// so that in case preprocess fails, no supplement will be created with empty media info
				preprocessService.preprocess(supplement, true);
				
				// Move the file from the dropbox to amppd file storage:
				// need to use originalFilename instead of batchFile.getPrimaryfileFilename() for source filename,
				// as the latter might have been converted from flac to wav during preprocess,
				// while the former would have been updated to the generated wav file in this case
				supplement.setPathname(fileStorageService.getFilePathname(supplement));				
				Path targetSuppPath = moveFile(sourceDir, supplement.getOriginalFilename(), supplement.getPathname());

				// save supplement after files are moved
				itemSupplementRepository.save(supplement);
				
				// Log that the file was created
				logFileCreated(supplement, targetSuppPath);
			}
		}
		catch(IOException ex) {
			throw new Exception(String.format("Error creating item supplement %s: %s", batchSupplementFile.getSupplementFilename(), ex.toString()), ex);
		}
	}
	
	/*
	 * Create an item supplememt if it does not already exists
	 */
	private ItemSupplement getItemSupplement(Item item, BatchSupplementFile batchSupplementFile, String username, List<String>errors) {
		ItemSupplement itemSupplement = null;
		if(item.getSupplements() != null) {
			log.info("BATCH PROCESSING : looping through the existing item supplement");
			for(ItemSupplement is : item.getSupplements()) {
				if(is.getName() != null && is.getName().contentEquals(batchSupplementFile.getSupplementName())) {
					log.error("BATCH PROCESSING : item supplement name already exists");
					errors.add("ERROR: In row "+currRow+" item supplement name already exists");
					itemSupplement = is;
					break;
				}
			}
		}
				
		// Create Supplement if doesn't already exists
		if(itemSupplement == null) {
			itemSupplement = new ItemSupplement();
			itemSupplement.setItem(item);
			itemSupplement.setName(batchSupplementFile.getSupplementName());
			itemSupplement.setOriginalFilename(batchSupplementFile.getSupplementFilename());
			itemSupplement.setDescription(batchSupplementFile.getSupplementDescription());
			itemSupplement.setCreatedBy(username);
			itemSupplement.setModifiedBy(username);
			itemSupplement.setCreatedDate(new Date());
			itemSupplement.setModifiedDate(new Date());
			log.info("BATCH PROCESSING : new item supplement created");
		}
		return itemSupplement;
	}
	
	/*
	 * Create a collection supplement if it does not already exists
	 */
	private CollectionSupplement getCollectionSupplement(Collection collection, BatchSupplementFile batchSupplementFile, String username, List<String>errors) {
		//check if the supplement exists
		CollectionSupplement collectionSupplement = null;
		if(collection.getSupplements() != null) {
			log.info("BATCH PROCESSING : looping through the existing collection supplements");
			for(CollectionSupplement cs : collection.getSupplements()) {
				if(cs.getName() != null && cs.getName().contentEquals(batchSupplementFile.getSupplementName())) {
					log.error("BATCH PROCESSING : collection supplement name already exists");
					errors.add("ERROR: In row "+ currRow +" collection supplement name already exists"); 
					collectionSupplement = cs;
					break;
				}
			}
		}
				
		// Create Supplement if doesn't already exists
		if(collectionSupplement == null) {
			collectionSupplement = new CollectionSupplement();
			collectionSupplement.setCollection(collection);
			collectionSupplement.setName(batchSupplementFile.getSupplementName());
			collectionSupplement.setOriginalFilename(batchSupplementFile.getSupplementFilename());
			collectionSupplement.setDescription(batchSupplementFile.getSupplementDescription());
			collectionSupplement.setCreatedBy(username);
			collectionSupplement.setModifiedBy(username);
			collectionSupplement.setCreatedDate(new Date());
			collectionSupplement.setModifiedDate(new Date());
			log.info("BATCH PROCESSING : new collection supplement created");
		}
		
		return collectionSupplement;
	}
	
	/*
	 * Create a primaryfile supplement
	 */
	private PrimaryfileSupplement createPrimaryfileSupplement(Primaryfile primaryfile, BatchSupplementFile batchSupplementFile, String username, List<String> errors) {
		//check if the supplement exists
		PrimaryfileSupplement primaryfileSupplement = null;
		if(primaryfile.getSupplements() != null) {
			log.info("BATCH PROCESSING : looping through the existing primaryfile supplements");
			for(PrimaryfileSupplement ps : primaryfile.getSupplements()) {
				if(ps.getName() != null && ps.getName().contentEquals(batchSupplementFile.getSupplementFilename())) {
					log.error("BATCH PROCESSING : primaryfile supplement name already exists");
					errors.add("ERROR: In row "+currRow+" primaryfile supplement name already exists");
					primaryfileSupplement = ps;
					break;
				}
			}
		}
		
		// Create Supplements
		if(primaryfileSupplement == null) {
			primaryfileSupplement = new PrimaryfileSupplement();
			primaryfileSupplement.setPrimaryfile(primaryfile);
			primaryfileSupplement.setName(batchSupplementFile.getSupplementName());
			primaryfileSupplement.setOriginalFilename(batchSupplementFile.getSupplementFilename());
			primaryfileSupplement.setDescription(batchSupplementFile.getSupplementDescription());
			primaryfileSupplement.setCreatedBy(username);
			primaryfileSupplement.setModifiedBy(username);
			primaryfileSupplement.setCreatedDate(new Date());
			primaryfileSupplement.setModifiedDate(new Date());
			log.info("BATCH PROCESSING : new primaryfile supplement created");
		}
		return primaryfileSupplement;
	}
	
	/*
	 * Either create a primaryfile or get an existing one 
	 */
	private Primaryfile getPrimaryfile(Collection batchfileCollection, Item item, BatchFile batchFile, String username, List<String> errors) {
		Primaryfile primaryfile =null;
		boolean found = false;
		Set <Primaryfile> primaryfiles = item.getPrimaryfiles();
		
		// TODO this search should be done with DB Repository, more efficient than java code
		// For primaryfiles in a given item, names must be unique.
		if(primaryfiles != null && primaryfiles.size() >= 0) {
			for(Primaryfile p : primaryfiles) { 
				if((p.getName() != null && p.getName().contentEquals(batchFile.getPrimaryfileName()) ) ) {
					// report duplicate error only if ingesting primaryfile, but not if only ingesting a supplement for it
					if (!batchFile.getPrimaryfileFilename().isBlank()) {
						log.error("BATCH PROCESSING : primaryfile name already exists");
						errors.add("ERROR: In row "+currRow+" primaryfile name already exists");
					}		
					found = true;
					primaryfile = p;
					break; 
				}
			}
		}
		
		// If no existing primaryfile
		if(!found) {
			// if this is ingesting primaryfile supplment only, we need an existing primaryfile
			if (batchFile.getSupplementType()==SupplementType.PRIMARYFILE && batchFile.getPrimaryfileFilename().isBlank()) {
				log.error("BATCH PROCESSING : primaryfile does't exist for ingesting primaryfile supplment only");
				errors.add("ERROR: In row "+currRow+" primaryfile does't exist for ingesting primaryfile supplment only");				
			}
			// otherwise create a new Primaryfile	
			else {					
				primaryfile = new Primaryfile();
				primaryfile.setName(batchFile.getPrimaryfileName());
				primaryfile.setDescription(batchFile.getPrimaryfileDescription());
				primaryfile.setOriginalFilename(batchFile.getPrimaryfileFilename());
				primaryfile.setCreatedBy(username);
				primaryfile.setCreatedDate(new Date());
				primaryfile.setModifiedBy(username);
				primaryfile.setModifiedDate(new Date());
				primaryfile.setItem(item);
				log.info("BATCH PROCESSING : created new primaryfile object");
			}
		}
		
		return primaryfile;				
	}
	
	/*
	 * Gets an Item object.  If one already exists, 
	 */
	private Item getItem(Collection collection, String itemName, String itemDescription, String createdBy, String externalId, String externalSource) {
		Item item = null;
		Set<Item> items = collection.getItems();
		boolean found = false;
		String newItemName = itemName;
		String delimiter = " - ";
		if(items!=null) {
			log.info("BATCH PROCESSING : check for matching item in this collection"); 
			for(Item i : items) {
				if(!externalId.isBlank()) {
					boolean matchesExternalId = i.getExternalId() != null && i.getExternalId().equals(externalId);
					boolean matchesExternalSource = externalSource.isBlank() || (i.getExternalSource()!=null && externalSource.equals(i.getExternalSource()));
					String expectedName = itemName + delimiter + externalSource + delimiter + externalId;
					if(matchesExternalId && matchesExternalSource) {
						found = true;
						item = i;
						// TODO: we are updating item name when not match but not updating when
						if(!i.getName().contentEquals(itemName) && !i.getName().contentEquals(expectedName)) {
							log.info("BATCH PROCESSING : External Item id already exists");
							if(itemRepository.findByCollectionIdAndName(collection.getId(), itemName).size() == 0) {
								itemRepository.updateName(itemName,i.getId());
							} else if(itemRepository.findByCollectionIdAndName(collection.getId(), expectedName).size() == 0) {
								itemRepository.updateName(expectedName,i.getId());
							} else {
								log.error("BATCH PROCESSING : Item with same names " + itemName +" and " + expectedName +" already existed");
								return null;
							}
							//batchValidationResponse.addProcessingError("ERROR: In row "+currRow+" Item name already exists")

						}
					} else if(i.getName().contentEquals(itemName) && !externalSource.isBlank()) {
						newItemName = expectedName;
					}
				}
				else if(i.getName().contentEquals(itemName)) {
					log.info("BATCH PROCESSING : Item name already exists"); 
					//batchValidationResponse.addProcessingError("ERROR: In row "+currRow+" Item name already exists");
					found = true;
					item = i;
				}
				if(found)
					break;
			}
		}
		// If item doesn't already exists
		if(item==null && !found) {
			item = new Item();
			item.setName(newItemName);
			if(!externalId.isBlank()) {
				item.setExternalId(externalId);
			}
			if(!externalSource.isBlank()) {
				item.setExternalSource(externalSource);
			}
			item.setDescription(itemDescription);
			item.setCreatedBy(createdBy);
			item.setModifiedBy(createdBy);
			item.setCreatedDate(new Date());
			item.setModifiedDate(new Date());
			item.setCollection(collection);
			if(itemRepository.findByCollectionIdAndName(collection.getId(), newItemName).size() == 0) {
				itemRepository.save(item);
			} else {
				log.error("BATCH PROCESSING : Item with same name (" + newItemName +") already existed");
				return null;
			}

			log.info("BATCH PROCESSING : Item was not found and hence new item was created"); 
		}
		
		return item;
	}
	
	/*
	 * Move the media and info files from dropbox to corresponding media directory.
	 */
	private Path moveFile(String sourceDir, String sourceFilename, String targetFilename) throws IOException {		
		// get source/target paths for media file /{root}/{unit}/{collection}/{filename}
		Path sourceMedia = Paths.get(sourceDir, sourceFilename);	
		Path targetMedia = fileStorageService.resolve(targetFilename);	

		// get source/target paths for media info json file
		Path sourceJson = Paths.get(preprocessService.getMediaInfoJsonPath(sourceMedia.toString()));
		Path targetJson = Paths.get(preprocessService.getMediaInfoJsonPath(targetMedia.toString()));		
		
		// move the media and info files
		// Note: 
		// At this point both source media and info files must exist, otherwise there would have been exception during preprocess.
		// If the original file is .flac, the converted .wav file will be moved instead
		targetMedia = fileStorageService.move(sourceMedia, targetMedia);
		targetJson = fileStorageService.move(sourceJson, targetJson);
		
		return targetMedia;
	}
	
	/*
	 * Methods for logging when a file was created
	 */
	private void logFileCreated(Primaryfile primaryfile, Path targetPath) {
    	log.info(String.format("Primaryfile %s has media file %s successfully uploaded to %s.", primaryfile.getId(), primaryfile.getOriginalFilename(), targetPath));
	}
	
	private void logFileCreated(PrimaryfileSupplement supplement, Path targetPath) {
    	log.info(String.format("Primaryfile supplement %s has media file %s successfully uploaded to %s.", supplement.getId(), supplement.getOriginalFilename(), targetPath));
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
		return dropboxService.getSubDirPath(unit.getName(), collection.getName()).toString();
	}
	
	// TODO 
	// The duplicate name checking logic in all the get*** methods in this class should be done with DB Repository.
	// Note that the processBatch loop check error on each row, but continue on following rows, 
	// which means a batch can be partially ingested.
	
	
}