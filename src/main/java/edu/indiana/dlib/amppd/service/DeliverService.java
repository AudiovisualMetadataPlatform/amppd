package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.web.AvalonRelatedItems;

/**
 * Service to deliver AMP deliverables to target systems.
 * @author yingfeng
 */
public interface DeliverService {

	/**
	 * Deliver final results associated with the given item to Avalon as related items of the corresponding media object.
	 * @param itemId ID of the given item
	 * @return the AvalonRelatedItems delivered
	 */
	public AvalonRelatedItems deliverAvalonItem(Long itemId);
	
	/**
	 * Deliver final results associated with the items within the given collection to target system Avalon.
	 * @param collectionId ID of the given collection
	 * @return the list of AvalonRelatedItems delivered
	 */
	public List<AvalonRelatedItems> deliverAvalonCollection(Long collectionId);
	
}
