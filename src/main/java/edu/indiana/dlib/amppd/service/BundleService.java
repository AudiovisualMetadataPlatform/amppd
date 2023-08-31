package edu.indiana.dlib.amppd.service;

import java.util.List;
import java.util.Set;

import edu.indiana.dlib.amppd.model.Bundle;

/**
 * Service for bundle related functions.
 * @author yingfeng
 *
 */
public interface BundleService {

	/**
	 * Find bundles satisfying the combined criteria of name, keyword and creator, if provided.
	 * Note: If name is provided, other fields will be ignored, since name is unique and serves as ID.
	 * @param name name of bundle
	 * @param keyword keyword in the bundle name 
	 * @param creator username of the bundle creator
	 * @return bundles satisfying the criteria
	 */
	public List<Bundle> findByNameKeywordCreator(String name, String keyword, String creator);	

	/**
	 * Find all named bundles, i.e. bundles with non-empty name and non-empty primaryfiles.
	 * @return all named bundles
	 */
	public List<Bundle> findAllNamed();	

//	/**
//	 * Find the bundle with the given name created by the current user.
//	 * @param name name of the bundle
//	 * @return the matching bundle if found, or null otherwise
//	 */
//	public Bundle findNamedByCurrentUser(String name);	

	/**
	 * Add the given primaryfile to the given bundle.
	 * @param bundle the given bundle
	 * @param primaryfileId ID of the given primaryfile
	 * @return the updated bundle
	 */
	public Bundle addPrimaryfile(Bundle bundle, Long primaryfileId);

	/**
	 * Delete the given primaryfile from the given bundle.
	 * @param bundle the given bundle
	 * @param primaryfileId ID of the given primaryfile
	 * @return the updated bundle
	 */
	public Bundle deletePrimaryfile(Bundle bundle, Long primaryfileId);

	/**
	 * Add the given primaryfile to the given bundle.
	 * @param bundleId ID of the given bundle
	 * @param primaryfileId ID of the given primaryfile
	 * @return the updated bundle
	 */
	public Bundle addPrimaryfile(Long bundleId, Long primaryfileId);

	/**
	 * Delete the given primaryfile from the given bundle.
	 * @param bundleId ID of the given bundle
	 * @param primaryfileId ID of the given primaryfile
	 * @return the updated bundle
	 */
	public Bundle deletePrimaryfile(Long bundleId, Long primaryfileId);

	/**
	 * Add the given primaryfiles to the given bundle.
	 * @param bundleId ID of the given bundle
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @return the updated bundle
	 */
	public Bundle addPrimaryfiles(Long bundleId, Long[] primaryfileIds);

	/**
	 * Delete the given primaryfiles from the given bundle.
	 * @param bundleId ID of the given bundle
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @return the updated bundle
	 */
	public Bundle deletePrimaryfiles(Long bundleId, Long[] primaryfileIds);

	/**
	 * Update (without persistence to DB) the given bundle with the given name, description and set of primaryfiles.
	 * @param bundle the bundle to be updated
	 * @param name new name of the bundle
	 * @param description new description of the bundle
	 * @param primaryfileIds IDs of the new set of primaryfiles
	 * @return the updated bundle
	 */
	public Bundle updateBundle(Bundle bundle, String name, String description, Long[] primaryfileIds);

	/**
	 * Update (without persistence to DB) the given bundle with the given name, description and set of primaryfiles.
	 * @param bundleId ID of the bundle to be updated
	 * @param name new name of the bundle
	 * @param description new description of the bundle
	 * @param primaryfileIds IDs of the new set of primaryfiles
	 * @return the updated bundle
	 */
	public Bundle updateBundle(Long bundleId, String name, String description, Long[] primaryfileIds);

	/**
	 * Create (without persistence to DB) a new bundle with the given name, description, and prifmaryfiles.
	 * @param name name of the new bundle
	 * @param description description of the new bundle
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @return the newly created bundle
	 */
	public Bundle createBundle(String name, String description, Long[] primaryfileIds);

	/**
	 * Filter (without persistence to DB) the given bundle, i.e. exclude its primaryfiles not in the given units.
	 * @param bundle bundle to be filtered
	 * @param acUnitIds IDs of the units to include
	 * @return true if none of the bundle's primaryfiles have been excluded; false otherwise
	 */
	public boolean filterBundle(Bundle bundle, Set<Long> acUnitIds);		
	
	/**
	 * Filter (without persistence to DB) the given list of bundles, i.e. include only primaryfiles in the given units for each bundle,
	 * and exclude empty bundles after filtering.
	 * @param bundles bundles to be filtered
	 * @param acUnitIds IDs of the units to include
	 * @return the filtered bundles
	 */
	public List<Bundle> filterBundles(List<Bundle> bundles, Set<Long> acUnitIds);		
	
}
