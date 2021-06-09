package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.DeliverService;
import edu.indiana.dlib.amppd.web.AvalonRelatedItems;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to deliver AMP deliverables to target systems.
 * @author yingfeng
 */
@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class DeliverController {

	@Autowired
	private DeliverService deliverService;

	/**
	 * Deliver final results associated with the given item to Avalon as related items of the corresponding media object.
	 * @param itemId ID of the given item
	 * @return the AvalonRelatedItems delivered
	 */
	@PostMapping("/deliver/avalon/item/{itemId}")
	public AvalonRelatedItems deliverAvalonItem(@PathVariable Long itemId) {
		log.info("Deliver final results to Avalon for item " + itemId + " ... " );
		return deliverService.deliverAvalonItem(itemId);
	}

	/**
	 * Deliver final results associated with the items within the given collection to target system Avalon.
	 * @param collectionId ID of the given collection
	 * @return the list of AvalonRelatedItems delivered
	 */
	@PostMapping("/deliver/avalon/collection/{collectionId}")
	public AvalonRelatedItems deliverAvalonCollection(@PathVariable Long collectionId) {
		log.info("Deliver final results to Avalon for items in collection " + collectionId + " ... " );
		return deliverService.deliverAvalonItem(collectionId);
	}

}