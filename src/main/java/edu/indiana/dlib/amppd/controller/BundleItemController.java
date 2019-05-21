package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import lombok.extern.java.Log;

/**
 * Controller for REST operations on Bundle.
 * @author yingfeng
 *
 */
@RestController
@Log
public class BundleItemController {

	@Autowired
    private BundleRepository bundleRepository;
	
	@Autowired
    private ItemRepository itemRepository;

	@PostMapping("/bundles/{bundleId}/add/items/{itemId}")
	public Bundle addItemToBundle(@PathVariable("bundleId") Long bundleId, @PathVariable("itemId") Long itemId) {		
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));    
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    

		// check if the item has been added to the bundle already
		if (bundle.getItems().contains(item)) {
			// TODO the warning message shall be displayed back to the screen
			String msg = "Item <" + itemId + "> has already been added to bundle<" + bundleId + ">!";
			log.warning(msg);	
			return bundle;
		}

		item.getBundles().add(bundle);	// need to add from item side since Item owns the M;M relationship
		bundle.getItems().add(item);	// TODO do we need this?
		itemRepository.save(item);
		bundleRepository.save(bundle);	// TODO do we need this?
		
		String msg = "You successfully added item <" + itemId + "> to bundle<" + bundleId + ">!";
		log.info(msg);
		return bundle;
    }

	@PostMapping("/bundles/{bundleId}/delete/items/{itemId}")
    public Bundle deleteItemFromBundle(@PathVariable("bundleId") Long bundleId, @PathVariable("itemId") Long itemId) {		
		Item item = itemRepository.findById(itemId).orElseThrow(() -> new StorageException("item <" + itemId + "> does not exist!"));    
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("bundle <" + bundleId + "> does not exist!"));    

		// check if the item exists in the bundle
		if (!bundle.getItems().contains(item)) {
			// TODO the warning message shall be displayed back to the screen
			String msg = "Item <" + itemId + "> doesn't exist bundle<" + bundleId + ">!";
			log.warning(msg);	
			return bundle;
		}

		item.getBundles().remove(bundle);	// need to remove from item side since Item owns the M;M relationship
		bundle.getItems().remove(item);		// TODO do we need this?
		itemRepository.save(item);
		bundleRepository.save(bundle);		// TODO do we need this?

    	String msg = "You successfully deleted item <" + itemId + "> from bundle<" + bundleId + ">!";
    	log.info(msg);
        return bundle;
    }
	

	// TODO We shall allow users to add/delete multiple items to a bundle at once; question is, how to represent an array of itemIds in the request?
	
	
}
