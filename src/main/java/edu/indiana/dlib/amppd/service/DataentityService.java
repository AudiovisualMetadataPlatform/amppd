package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;

/**
 * Service for common Dataentitiy operations. 
 * @author yingfeng
 */
public interface DataentityService {

	/**
	 * Get the URL for the given dataentity.
	 * @param dataentity the given dataentity
	 * @return the generated URL
	 */
	public String getDataentityUrl(Dataentity dataentity);

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
	 * Find the parent dataentity with the given ID for a supplement of the given type.
	 * @param id ID of the parent dataentity
	 * @param type type of the supplement
	 * @return the parent dataentity
	 */
	public Dataentity findParentDataEntity(Long id, SupplementType type);
	
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
	public List<? extends Dataentity> findDuplicateDataentitiesByExternalSrcAndId(Dataentity dataentity);

	/**
	 * Find the non-asset dataentity with the given ID and type from DB.
	 * @param id ID of the queried dataentity
	 * @param clazz class of the queried dataentity
	 * @return the non-asset dataentity with the given ID and type
	 */
	public Dataentity findNonAssetDataentity(Long id, Class clazz);

	/**
	 * Find the asset with the given ID and type from DB.
	 * @param id ID of the given asset
	 * @param type SupplementType of the given asset
	 * @return the found asset
	 */
	public Asset findAsset(Long id, SupplementType type);

	/**
	 * Save the given asset to DB.
	 * @param asset the given asset
	 * @return the saved asset
	 */
	public Asset saveAsset(Asset asset);
	
	/**
	 * Get the SupplementType of the given supplement.
	 * @param supplement the given supplement
	 * @return SupplementType of the given supplement
	 */
	public SupplementType getSupplementType(Supplement supplement);
	
	/**
	 * Change the parent of the given supplement into the given new parent dataentity; 
	 * If the new parent is of a different type, the old supplement will be deleted and a new one of the new type will be created.
	 * Note that this method doesn't perform any DB or file system updates.
	 * @param supplement the supplement to be moved
	 * @param parent the new parent of the supplement
	 * @return the updated supplement after moving 
	 */
	public Supplement changeSupplementParent(Supplement supplement, Dataentity parent);	
	
	/**
	 * Delete the given supplement from DB.
	 * @param supplement the supplement to be deleted
	 */
	public void deleteSupplement(Supplement supplement);
	
	/**
	 * Move the given supplement into the given parent dataentity, deleting the old and creating a new supplement if type changes,
	 * move its asset into the new parent's folder, and save the updated supplement.
	 * @param supplementId ID of the supplement to be moved
	 * @param supplementType type of the original supplement
	 * @param parentId ID of the new parent of the supplement
	 * @param parentType type of the new parent of the supplement
	 * @return the updated supplement after moving 
	 */
	public Supplement moveSupplement(Long supplementId, SupplementType supplementType, Long parentId, String parentType);	

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
	 * Get all supplements associated with the primaryfiles at all parent levels, with the given supplement name, category and format.
	 * @param primaryfiles the given primaryfiles
	 * @param name name of the supplement
	 * @param category category of the supplement
	 * @param format format of the supplement
	 * @return list of all supplements satisfying the criteria for each primaryfile
	 */
	public List<List<Supplement>> getSupplementsForPrimaryfiles(List<Primaryfile> primaryfiles, String name, String category, String format);
	
}
