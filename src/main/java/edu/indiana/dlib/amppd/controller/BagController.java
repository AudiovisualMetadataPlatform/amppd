package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionBag;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemBag;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileBag;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.BagService;
import edu.indiana.dlib.amppd.service.PermissionService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to handle requests related to OutputBag at various levels.
 * @author yingfeng
 */
@RestController
@Slf4j
public class BagController {

	@Autowired
	private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired
	private CollectionRepository collectionRepository;
	
	@Autowired
	private PermissionService permissionService;
	
	@Autowired
	private BagService bagService;

	
	/**
	 * Gets the PrimaryfileBag associated with the given primaryfile.
	 * @param primaryfileId ID of the given primaryfile
	 * @return the PrimaryfileBag retrieved
	 */
	@GetMapping(path = "/bags/primaryfile/{primaryfileId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public PrimaryfileBag getPrimaryfileBag(@PathVariable Long primaryfileId) {
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));    		
		Long acUnitId = primaryfile.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Bag, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot retrieve primaryfile bags.");
		}
		
		log.info("Getting PrimaryfileBag for primaryfileId " + primaryfileId + " ... " );
		return bagService.getPrimaryfileBag(primaryfile);		
	}
	
	/**
	 * Gets the ItemBag associated with the given item.
	 * @param itemId ID of the given item
	 * @return the ItemBag retrieved
	 */
	@GetMapping(path = "/bags/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBag getItemBag(@PathVariable Long itemId) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));    
		Long acUnitId = item.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Bag, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot retrieve item bags.");
		}
		
		log.info("Getting ItemBag for itemId " + itemId + " ... " );
		return bagService.getItemBag(itemId);		
	}

	/**
	 * Gets the ItemBag associated with the given item.
	 * @param externalSource externalSource of the given item
	 * @param externalId externalId of the given item
	 * @return the ItemBag retrieved
	 */
	// Disable unused Endpoint
//	@GetMapping(path = "/bags/item", produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBag getItemBag(@RequestParam String externalSource, @RequestParam String externalId) {
		log.info("Getting ItemBag for external source-id " + externalSource + "-" + externalId + " ...");
		return bagService.getItemBag(externalSource, externalId);		
	}
	
	/**
	 * Gets the CollectionBag associated with the given collection.
	 * @param collectionId ID of the given collection
	 * @return the CollectionBag retrieved
	 */
	@GetMapping(path = "/bags/collection/{collectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public CollectionBag getCollectionBag(@PathVariable Long collectionId) {
		Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new StorageException("collection <" + collectionId + "> does not exist!"));    		
		Long acUnitId = collection.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Bag, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot retrieve collection bags.");
		}
		
		log.info("Getting CollectionBag for collectionId " + collectionId + " ... " );
		return bagService.getCollectionBag(collectionId);		
	}
	
	/**
	 * Gets the CollectionBag associated with the given collection.
	 * @param unitName name of the given collection's parent unit
	 * @param collectionName name of the given collection
	 * @return the CollectionBag retrieved
	 */
	// Disable unused Endpoint
//	@GetMapping(path = "/bags/item", produces = MediaType.APPLICATION_JSON_VALUE)
	@GetMapping(path = "/bags/collection", produces = MediaType.APPLICATION_JSON_VALUE)
	public CollectionBag getCollectionBag(@RequestParam String unitName, @RequestParam String collectionName) {
		log.info("Getting CollectionBag for unitName-collectionName " + unitName + "-" + collectionName);
		return bagService.getCollectionBag(unitName, collectionName);		
	}
	
}
