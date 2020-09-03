package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.CollectionBag;
import edu.indiana.dlib.amppd.model.ItemBag;
import edu.indiana.dlib.amppd.model.PrimaryfileBag;

/**
 * Service for BagOutput related functions. 
 * @author yingfeng
 *
 */
public interface BagService {

	/**
	 * Gets the PrimaryfileBag associated with the given primaryfile.
	 * @param primaryfileId ID of the given primaryfile
	 * @return the PrimaryfileBag retrieved
	 */
	public PrimaryfileBag getPrimaryfileBag(Long primaryfileId);
	
	/**
	 * Gets the ItemBag associated with the given item.
	 * @param itemId ID of the given item
	 * @return the ItemBag retrieved
	 */
	public ItemBag getItemBag(Long itemId);

	/**
	 * Gets the ItemBag associated with the given item.
	 * @param externalSource externalSource of the given item
	 * @param externalId externalId of the given item
	 * @return the ItemBag retrieved
	 */
	public ItemBag getItemBag(String externalSource, String externalId);

	/**
	 * Gets the CollectionBag associated with the given collection.
	 * @param collectionId ID of the given collection
	 * @return the CollectionBag retrieved
	 */
	public CollectionBag getCollectionBag(Long collectionId);

	/**
	 * Gets the CollectionBag associated with the given collection.
	 * @param unitName name of the given collection's parent unit
	 * @param collectionName name of the given collection
	 * @return the CollectionBag retrieved
	 */
	public CollectionBag getCollectionBag(String unitName, String collectionName);

}
