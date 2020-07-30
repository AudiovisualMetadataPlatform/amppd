package edu.indiana.dlib.amppd.service;

import java.util.Set;

import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Primaryfile;

/**
 * Service for bundle related functions.
 * @author yingfeng
 *
 */
public interface BundleService {

	/**
	 * Find the bundle with the given name created by the current user.
	 * @param name name of the bundle
	 * @return the matching bundle if found, or null otherwise
	 */
	public Bundle findByNameCreatedByCurrentUser(String name);	
	
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
	  * Create a new bundle with the given name and prifmaryfiles.
	  * @param name name of the new bundle
	  * @param prifmaryfiles prifmaryfiles contained in the new bundle
	  * @return the newly created bundle
	  */
	 public Bundle createBundle(String name, Set<Primaryfile> prifmaryfiles);
	 
}
