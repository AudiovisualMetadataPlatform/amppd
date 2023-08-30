package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.dto.AvalonRelatedItems;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.service.DeliverService;
import edu.indiana.dlib.amppd.service.PermissionService;
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
	private CollectionRepository collectionRepository;

	@Autowired
	private DeliverService deliverService;
	
	@Autowired
	private PermissionService permissionService;
	
	
	/**
	 * Deliver final results associated with the given item to Avalon as related items of the corresponding media object.
	 * @param itemId ID of the given item
	 * @return the AvalonRelatedItems delivered
	 */
	@PostMapping(path = "/deliver/avalon/item/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public AvalonRelatedItems deliverAvalonItem(@PathVariable Long itemId) {
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));
		Long acUnitId = item.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Create, TargetType.Bag, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot deliver item bags in unit " + acUnitId);
		}

		log.info("Deliver final results to Avalon for item " + itemId + " ... " );
		return deliverService.deliverAvalonItem(item);
	}

	/**
	 * Deliver final results associated with the items within the given collection to Avalon.
	 * @param collectionId ID of the given collection
	 * @return the list of AvalonRelatedItems delivered
	 */
	@PostMapping(path = "/deliver/avalon/collection/{collectionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<AvalonRelatedItems> deliverAvalonCollection(@PathVariable Long collectionId) {
		Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new StorageException("collection <" + collectionId + "> does not exist!"));    
		Long acUnitId = collection.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Create, TargetType.Bag, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot deliver collection bags in unit " + acUnitId);
		}
		
		log.info("Deliver final results to Avalon for items in collection " + collectionId + " ... " );
		return deliverService.deliverAvalonCollection(collection);
	}

}