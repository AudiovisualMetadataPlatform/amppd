package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.dto.AvalonRelatedItems;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.service.DeliverService;
import edu.indiana.dlib.amppd.service.impl.DeliverServiceImpl;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to deliver AMP deliverables to target systems.
 * @author yingfeng
 */
//@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class DeliverController {

	@Autowired
	private ItemRepository itemRepository;

	@Autowired
	private DeliverService deliverService;

	/**
	 * Deliver final results associated with the given item to Avalon as related items of the corresponding media object.
	 * @param itemId ID of the given item
	 * @return the AvalonRelatedItems delivered
	 */
	@PostMapping(path = "/deliver/avalon/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public AvalonRelatedItems deliverAvalonItem(@PathVariable Long itemId) {
		log.info("Deliver final results to Avalon for item " + itemId + " ... " );
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));

		// verify that the item's collection has a valid externalId for Avalon
		Collection collection = item.getCollection();
		if (collection == null) {
			throw new RuntimeException("Collection for item " + itemId + " is null");
		}
		else {
			String collectionExternalId = collection.getExternalId();    
			if (!DeliverServiceImpl.AVALON.equalsIgnoreCase(collection.getExternalSource()) || StringUtils.isEmpty(collectionExternalId)) {
				throw new RuntimeException("Collection " + collection.getId() + " for item " + itemId + " has invalid external source or ID for Avalon");
			}
			return deliverService.deliverAvalonItem(itemId, collectionExternalId);
		}		
	}

	/**
	 * Deliver final results associated with the items within the given collection to Avalon.
	 * @param collectionId ID of the given collection
	 * @return the list of AvalonRelatedItems delivered
	 */
	@PostMapping(path = "/deliver/avalon/collection/{collectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<AvalonRelatedItems> deliverAvalonCollection(@PathVariable Long collectionId) {
		log.info("Deliver final results to Avalon for items in collection " + collectionId + " ... " );
		return deliverService.deliverAvalonCollection(collectionId);
	}

}