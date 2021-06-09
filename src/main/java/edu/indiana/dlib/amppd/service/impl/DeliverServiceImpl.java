package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.BagContent;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemBag;
import edu.indiana.dlib.amppd.model.PrimaryfileBag;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.service.BagService;
import edu.indiana.dlib.amppd.service.DeliverService;
import edu.indiana.dlib.amppd.web.AvalonRelatedItems;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of DeliverService.
 * @author yingfeng
 */
@Service
@Slf4j
public class DeliverServiceImpl implements DeliverService {
		
	// external source value for Avalon
	public static final String AVALON = "avalon"; 
	
	@Autowired
	private CollectionRepository collectionRepository;
	
	@Autowired
	private BagService bagService;
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DeliverService.deliverAvalonItem(Long)
	 */
	public AvalonRelatedItems deliverAvalonItem(Long itemId) {
		ItemBag itemBag = bagService.getItemBag(itemId);
		AvalonRelatedItems aris = new AvalonRelatedItems();			
		aris.setUrls(new ArrayList<String>());		
		aris.setLabels(new ArrayList<String>());		
		
		// verify that the item has a valid externalId for Avalon
		if (!AVALON.equalsIgnoreCase(itemBag.getExternalSource()) || StringUtils.isEmpty(itemBag.getExternalId())) {
			throw new RuntimeException("Item " + itemId + " has invalid external source or ID for Avalon");
		}
		
		// go through each BagContent contained in the itemBag
		for (PrimaryfileBag pb : itemBag.getPrimaryfileBags()) {
			for (BagContent bc : pb.getBagContents()) {
				aris.getUrls().add(bc.getOutputUrl());	// TODO we might prefer symlink
				aris.getLabels().add("AMP " + bc.getOutputType().toUpperCase() + " - " + pb.getPrimaryfileName() + " - " + bc.getDateCreated());
			}
		}
				
		// call Avalon API to link related items to the corresponding media object
		putAvalonRelatedItems(itemBag.getExternalId(), aris);
		
		log.info("Successfully delivered " + aris.getLabels().size() + " final results for item " + itemId + " to Avalon.");
		return aris;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DeliverService.deliverAvalonCollection(Long)
	 */
	public List<AvalonRelatedItems> deliverAvalonCollection(Long collectionId) {
		List<AvalonRelatedItems> ariss = new ArrayList<AvalonRelatedItems>();
		Collection collection = collectionRepository.findById(collectionId).orElseThrow(() -> new StorageException("collection <" + collectionId + "> does not exist!"));    

		for (Item item : collection.getItems()) {
			ariss.add(this.deliverAvalonItem(item.getId()));
		}
		
		log.info("Successfully delivered final results for " + ariss.size() + " items in collection " + collectionId + " to Avalon.");
		return ariss;
	}

	/**
	 * @see
	 */
	public String putAvalonRelatedItems(String externalId, AvalonRelatedItems aris) {
		return null;
	}
	
}
