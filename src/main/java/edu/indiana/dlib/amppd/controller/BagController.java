package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.CollectionBag;
import edu.indiana.dlib.amppd.model.ItemBag;
import edu.indiana.dlib.amppd.model.PrimaryfileBag;
import edu.indiana.dlib.amppd.service.BagService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to handle requests related to OutputBag at various levels.
 * @author yingfeng
 */
//@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class BagController {

	@Autowired
	private BagService bagService;

	/**
	 * Gets the PrimaryfileBag associated with the given primaryfile.
	 * @param primaryfileId ID of the given primaryfile
	 * @return the PrimaryfileBag retrieved
	 */
	@GetMapping(path = "/bags/primaryfile/{primaryfileId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public PrimaryfileBag getPrimaryfileBag(@PathVariable Long primaryfileId) {
		log.info("Getting PrimaryfileBag for primaryfileId " + primaryfileId + " ... " );
		return bagService.getPrimaryfileBag(primaryfileId);		
	}
	
	/**
	 * Gets the ItemBag associated with the given item.
	 * @param itemId ID of the given item
	 * @return the ItemBag retrieved
	 */
	@GetMapping(path = "/bags/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemBag getItemBag(@PathVariable Long itemId) {
		log.info("Getting ItemBag for itemId " + itemId + " ... " );
		return bagService.getItemBag(itemId);		
	}

	/**
	 * Gets the ItemBag associated with the given item.
	 * @param externalSource externalSource of the given item
	 * @param externalId externalId of the given item
	 * @return the ItemBag retrieved
	 */
	@GetMapping(path = "/bags/item", produces = MediaType.APPLICATION_JSON_VALUE)
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
		log.info("Getting CollectionBag for collectionId " + collectionId + " ... " );
		return bagService.getCollectionBag(collectionId);		
	}
	
	/**
	 * Gets the CollectionBag associated with the given collection.
	 * @param unitName name of the given collection's parent unit
	 * @param collectionName name of the given collection
	 * @return the CollectionBag retrieved
	 */
	@GetMapping(path = "/bags/collection", produces = MediaType.APPLICATION_JSON_VALUE)
	public CollectionBag getCollectionBag(@RequestParam String unitName, @RequestParam String collectionName) {
		log.info("Getting CollectionBag for unitName-collectionName " + unitName + "-" + collectionName);
		return bagService.getCollectionBag(unitName, collectionName);		
	}
	
}
