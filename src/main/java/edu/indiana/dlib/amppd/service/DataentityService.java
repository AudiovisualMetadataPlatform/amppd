package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.Dataentity;

/**
 * Service for common Dataentitiy operations. 
 * @author yingfeng
 */
public interface DataentityService {

	/**
	 * Return all task managers supported by AMP defined in application configuration.
	 * @return the array of allowed task managers
	 */
	public String[] getAllowedTaskManagers();
	
	/**
	 * Return all external sources supported by AMP defined in application configuration.
	 * @return the array of allowed external sources 
	 */
	public String[] getAllowedExternalSources();
	
	/**
	 * Get the URL for the given dataentity.
	 * @param dataentity the given dataentity
	 * @return the generated URL
	 */
	public String getDataentityUrl(Dataentity dataentity);

}
