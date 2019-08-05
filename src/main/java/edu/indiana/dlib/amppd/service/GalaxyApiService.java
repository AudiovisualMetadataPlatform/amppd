package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.GalaxyWorkflow;

/**
 * Service to provide various requests to Galaxy instance via Galaxy REST API. 
 * @author yingfeng
 *
 */
public interface GalaxyApiService {
    
	/**
	 * Retrieve the API key for the current user from Galaxy. The key is used as a token for every REST request made to Galaxy.
	 * @return
	 */
	public String getApiKey();
		
	/**
	 * Retrieve all current workflows from Galaxy through its REST API.
	 * @return
	 */
	public GalaxyWorkflow[] getWorkflows();

}
