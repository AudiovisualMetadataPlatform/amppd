package edu.indiana.dlib.amppd.service;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;

import edu.indiana.dlib.amppd.model.galaxy.GalaxyUser;

/**
 * Service to provide convenient application level helpers to access Galaxy REST API.  
 * @author yingfeng
 */
public interface GalaxyApiService {
    
	// TODO further divide Galaxy related services into each own class, for ex, all workflow related methods can be moved into a separate GalaxyWorkflowService.
	
	/**
	 * Retrieve the Galaxy user information for the current AMP user.
	 * @return
	 */
	public GalaxyUser getCurrentUser();
	
	/**
	 * Returns Galaxy instance for the current user.
	 */
	public GalaxyInstance getInstance();

	/**
	 * Retrieve the API key for the current user from Galaxy. The key is used as a token for every REST request made to Galaxy.
	 * @return
	 */
	public String getApiKey();
		
}
