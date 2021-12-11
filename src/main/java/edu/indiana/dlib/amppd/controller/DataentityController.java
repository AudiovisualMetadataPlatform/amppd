package edu.indiana.dlib.amppd.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.impl.DataentityServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on Dataentity.
 * @author yingfeng
 */
@RestController
@Slf4j
public class DataentityController {
	
	@Autowired
	private DataentityService dataentityService;
	
	@Autowired
	private FileStorageService fileStorageService;
	
	@Autowired
	private CollectionRepository collectionRepository;
	
	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired
	private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
	private CollectionSupplementRepository collectionSupplementRepository;

	@Autowired
	private ItemSupplementRepository itemSupplementRepository;
	
	@Autowired
	private PrimaryfileSupplementRepository primaryfileSupplementRepository;

//	@Autowired
//	private CollectionRepository collectionRepository;
	
	/**
	 * Return the requested configuration properties.
	 * @param properties name of the properties requested; null means all client visible properties.
	 * @return a map of property name-value pairs
	 */
	@GetMapping("/config")
	public Map<String,List<String>> getConfigProperties(@RequestParam(required = false) List<String> properties) {
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		
		// Currently, client visible config properties include only externalSources and taskManagers;
		// all other ones requested are ignored.
		if (properties == null || properties.contains(DataentityServiceImpl.EXTERNAL_SOURCES)) {
			log.info("Getting configuration property " + DataentityServiceImpl.EXTERNAL_SOURCES);
			map.put(DataentityServiceImpl.EXTERNAL_SOURCES, dataentityService.getExternalSources());
		}
		if (properties == null || properties.contains(DataentityServiceImpl.TASK_MANAGERS)) {
			log.info("Getting configuration property " + DataentityServiceImpl.TASK_MANAGERS);
			map.put(DataentityServiceImpl.TASK_MANAGERS, dataentityService.getTaskManagers());
		}
		
		return map;
	}
	
	/**
	 * Create the given primaryfile with the given media file and add it to the given parent item.
	 * @param itemId ID of the given item
	 * @param primaryfile the given primaryfile
	 * @param mediaFile the media file content to be uploaded for the primaryfile
	 * @return the added primaryfile
	 */
	@PostMapping(path = "/items/{itemId}/addPrimaryfile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Primaryfile addPrimaryfile(@PathVariable Long itemId, @Valid @RequestPart Primaryfile primaryfile, @RequestPart MultipartFile mediaFile) {		
    	log.info("Adding primaryfile " + primaryfile.getName() + " under item " + itemId);
    	
    	// populate primaryfile.item in case it's not specified
    	if (primaryfile.getItem() == null) {
			Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));
			primaryfile.setItem(item);
    	}
    	
		// save primaryfile to DB 
		primaryfile = primaryfileRepository.save(primaryfile);
		
		// ingest media file after primaryfile is saved
		if (mediaFile == null) {
			throw new RuntimeException("No media file is provided for the primaryfile to be added.");
		}
		primaryfile = fileStorageService.uploadPrimaryfile(primaryfile, mediaFile);
		
    	return primaryfile;
    }
	
	/**
	 * Create the given collection supplement with the given media file and add it to the given parent collection.
	 * @param collectionId ID of the given collection
	 * @param collectionSupplement the given collection supplement
	 * @param mediaFile the media file content to be uploaded for the collection supplement
	 * @return the added collection supplement
	 */
	@PostMapping(path = "/collections/{collectionId}/addCollectionSupplement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CollectionSupplement addCollectionSupplement(@PathVariable Long collectionId, @Valid @RequestPart CollectionSupplement collectionSupplement, @RequestPart MultipartFile mediaFile) {		
    	log.info("Adding collectionSupplement " + collectionSupplement.getName() + " under collection " + collectionId);
    	
    	// populate collectionSupplement.collection in case it's not specified
    	if (collectionSupplement.getCollection() == null) {
    		Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new StorageException("collection <" + collectionId + "> does not exist!"));
    		collectionSupplement.setCollection(collection);
    	}
    	
		// save collectionSupplement to DB 
    	collectionSupplement = collectionSupplementRepository.save(collectionSupplement);
		
		// ingest media file after collection is saved
		if (mediaFile == null) {
			throw new RuntimeException("No media file is provided for the collectionSupplement to be added.");
		}
		collectionSupplement = fileStorageService.uploadCollectionSupplement(collectionSupplement, mediaFile);
		
    	return collectionSupplement;
    }
	
	/**
	 * Create the given item supplement with the given media file and add it to the given parent item.
	 * @param itemId ID of the given item
	 * @param itemSupplement the given item supplement
	 * @param mediaFile the media file content to be uploaded for the item supplement
	 * @return the added item supplement
	 */
	@PostMapping(path = "/items/{itemId}/addItemSupplement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ItemSupplement addItemSupplement(@PathVariable Long itemId, @Valid @RequestPart ItemSupplement itemSupplement, @RequestPart MultipartFile mediaFile) {		
    	log.info("Adding itemSupplement " + itemSupplement.getName() + " under item " + itemId);
    	
    	// populate itemSupplement.item in case it's not specified
    	if (itemSupplement.getItem() == null) {
    		Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));
    		itemSupplement.setItem(item);
    	}
    	
		// save itemSupplement to DB 
    	itemSupplement = itemSupplementRepository.save(itemSupplement);
		
		// ingest media file after item is saved
		if (mediaFile == null) {
			throw new RuntimeException("No media file is provided for the itemSupplement to be added.");
		}
		itemSupplement = fileStorageService.uploadItemSupplement(itemSupplement, mediaFile);
		
    	return itemSupplement;
    }
	
	/**
	 * Create the given primaryfile supplement with the given media file and add it to the given parent primaryfile.
	 * @param primaryfileId ID of the given primaryfile
	 * @param primaryfileSupplement the given primaryfile supplement
	 * @param mediaFile the media file content to be uploaded for the primaryfile supplement
	 * @return the added primaryfile supplement
	 */
	@PostMapping(path = "/primaryfiles/{primaryfileId}/addPrimaryfileSupplement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public PrimaryfileSupplement addPrimaryfileSupplement(@PathVariable Long primaryfileId, @Valid @RequestPart PrimaryfileSupplement primaryfileSupplement, @RequestPart MultipartFile mediaFile) {		
    	log.info("Adding primaryfileSupplement " + primaryfileSupplement.getName() + " under primaryfile " + primaryfileId);
    	
    	// populate primaryfileSupplement.primaryfile in case it's not specified
    	if (primaryfileSupplement.getPrimaryfile() == null) {
    		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));
    		primaryfileSupplement.setPrimaryfile(primaryfile);
    	}
    	
		// save primaryfileSupplement to DB 
    	primaryfileSupplement = primaryfileSupplementRepository.save(primaryfileSupplement);
		
		// ingest media file after primaryfile is saved
		if (mediaFile == null) {
			throw new RuntimeException("No media file is provided for the primaryfileSupplement to be added.");
		}
		primaryfileSupplement = fileStorageService.uploadPrimaryfileSupplement(primaryfileSupplement, mediaFile);
		
    	return primaryfileSupplement;
    }
	
	
	
	
//	@PostMapping(path = "/collections/{id}/activate")
//	public Collection activateCollection(@PathVariable Long id, @RequestParam Boolean active){
//		log.info("Activating collection "  + id + ": " + active);		
//		Collection collection = collectionRepository.findById(id).orElseThrow(() -> new StorageException("Collection <" + id + "> does not exist!"));
//		collection.setActive(active);
//		collectionRepository.save(collection);
//		log.info("Successfully activated collection " + id + ": " + active);	
//		return collection;
//	}
	
}
