package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement;

/**
 * Service for common Dataentitiy operations. 
 * @author yingfeng
 */
public interface DataentityService {

	/**
	 * Return all supplement categories handled by AMP defined in application configuration.
	 * @return the array of allowed supplement categories
	 */
	public List<String> getSupplementCategories();
	
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
	public List<? extends Dataentity> findDuplicateDataentitiesByName(Dataentity dataentity);

	/**
	 * Find the duplicate dataentities, i.e. those with the same external source, id (if exists) and name as the given dataentity, from DB.
	 * @param dataentity the given dataentity
	 * @return the list of duplicate dataentities found
	 */
	public List<? extends Dataentity> findDuplicateDataentitiesByNameByExternalSrcAndId(Dataentity dataentity);

	/**
	 * Get the given dataentity's parent dataentity.
	 * @param dataentity the given dataentity
	 * @return the parent dataentity
	 */
	public Dataentity getParentDataentity(Dataentity dataentit);	

	/**
	 * Set the given dataentity's parent to the given parent dataentity.
	 * @param dataentity the given dataentity
	 * @param parent the given parent dataentity
	 */
	public void setParentDataentity(Dataentity dataentity, Dataentity parent);	

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
	
	/**
	 * Get all supplements associated with the given primaryfile at all parent levels, with the given supplement name, category and format.
	 * @param primaryfile the given primaryfile
	 * @param name name of the supplement
	 * @param category category of the supplement
	 * @param format format of the supplement
	 * @return all supplements satisfying the criteria
	 */
	public List<Supplement> getSupplementsForPrimaryfile(Primaryfile primaryfile, String name, String category, String format);

	/**
	 * Get all supplements associated the primaryfiles at all parent levels, with the given supplement name, category and format.
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @param name name of the supplement
	 * @param category category of the supplement
	 * @param format format of the supplement
	 * @return list of all supplements satisfying the criteria for each primaryfile
	 */
	public List<List<Supplement>> getSupplementsForPrimaryfiles(Long[] primaryfileIds, String name, String category, String format);

}
