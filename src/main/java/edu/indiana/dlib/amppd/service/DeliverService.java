package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.web.AvalonMediaObject;
import edu.indiana.dlib.amppd.web.AvalonRelatedItems;

/**
 * Service to deliver AMP deliverables to target systems.
 * @author yingfeng
 */
public interface DeliverService {

	/**
	 * Deliver final results associated with the given item to Avalon as related items of the corresponding media object,
	 * providing the given externalID of the collection containing the given item.
	 * @param itemId ID of the given item
	 * @param collectionExternalId externalID of the collection containing the given item
	 * @return the AvalonRelatedItems delivered
	 */
	public AvalonRelatedItems deliverAvalonItem(Long itemId, String collectionExternalId);
	
	/**
	 * Deliver final results associated with the items within the given collection to target system Avalon.
	 * @param collectionId ID of the given collection
	 * @return the list of AvalonRelatedItems delivered
	 */
	public List<AvalonRelatedItems> deliverAvalonCollection(Long collectionId);
	
	/**
	 * Put (link) the AMP outputs contained in the AvalonRelatedItems to the Avalon media object corresponding to the given AMP item.
	 * @param externalId the external ID of the AMP item corresponding to the Avalon media object
	 * @param aris the AvalonRelatedItems containing AMP outputs for the item
	 */
	public AvalonMediaObject putAvalonRelatedItems(String externalId, AvalonRelatedItems aris);

	/**
	 * Get Avalon media object URL for the given AMP item.
	 * @param externalId the AMP item external ID corresponding to the Avalon media object ID
	 */
	public String getAvalonMediaObjectUrl(String externalId);
	
}
