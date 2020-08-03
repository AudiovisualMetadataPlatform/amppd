package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.Bundle;

/**
 * Service for bundle related functions.
 * @author yingfeng
 *
 */
public interface BundleService {

	/**
	 * Find all named bundles, i.e. bundles with non-empty name.
	 * @return all named bundles
	 */
	public List<Bundle> findAllNamed();	

	/**
	 * Find the bundle with the given name created by the current user.
	 * @param name name of the bundle
	 * @return the matching bundle if found, or null otherwise
	 */
	public Bundle findNamedByCurrentUser(String name);	

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
	 * Update the given bundle with the given description and set of primaryfiles.
	 * @param bundleId ID of the given bundle
	 * @param description description of the given bundle
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @return the updated bundle
	 */
	public Bundle updateBundle(Long bundleId, String description, Long[] primaryfileIds);

	/**
	 * Create a new bundle with the given name, description, and prifmaryfiles.
	 * @param name name of the new bundle
	 * @param description description of the new bundle
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @return the newly created bundle
	 */
	public Bundle createBundle(String name, String description, Long[] primaryfileIds);

}
