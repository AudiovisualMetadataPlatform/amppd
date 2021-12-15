package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Supplement;

/**
 * Service for common Dataentitiy operations. 
 * @author yingfeng
 */
public interface DataentityService {

	/**
	 * Return all external sources supported by AMP defined in application configuration.
	 * @return the array of allowed external sources 
	 */
	public List<String> getExternalSources();
	
	/**
	 * Return all task managers supported by AMP defined in application configuration.
	 * @return the array of allowed task managers
	 */
	public List<String> getTaskManagers();
	
	/**
	 * Get the URL for the given dataentity.
	 * @param dataentity the given dataentity
	 * @return the generated URL
	 */
	public String getDataentityUrl(Dataentity dataentity);

	/**
	 * Find the original dataentity with the same ID as the given dataentity from DB.
	 * @param dataentity the given dataentity
	 * @return the dataentity found
	 */
	public Dataentity findOriginalDataentity(Dataentity dataentity);

	/**
	 * Find the duplicate dataentities, i.e. those with the same parent (if exists) and name as the given dataentity, from DB.
	 * @param dataentity the given dataentity
	 * @return the list of duplicate dataentities found
	 */
	public List<? extends Dataentity> findDuplicateDataentities(Dataentity dataentity);

	/**
	 * Find the asset with the given ID and type from DB.
	 * @param id ID of the given asset
	 * @param type SupplementType of the given asset
	 * @return the found asset
	 */
	public Asset findAsset(Long id, Supplement.SupplementType type);

	/**
	 * Save the given asset to DB.
	 * @param asset the given asset
	 * @return the saved asset
	 */
	public Asset saveAsset(Asset asset);
	
}
