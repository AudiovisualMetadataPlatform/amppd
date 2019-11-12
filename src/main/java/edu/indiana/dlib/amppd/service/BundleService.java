package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.Bundle;

/**
 * Service for bundle related functions.
 * @author yingfeng
 *
 */
public interface BundleService {

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

	 
}
