package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.galaxy.GalaxyWorkflow;

/**
 * Service to provide various requests to Galaxy instance via Galaxy REST API. 
 * @author yingfeng
 */
public interface GalaxyApiService {
    
	// TODO further divide Galaxy related services into each own class, for ex, all workflow related methods can be moved into a separate GalaxyWorkflowService.
	
	/**
	 * Retrieve the API key for the current user from Galaxy. The key is used as a token for every REST request made to Galaxy.
	 * @return
	 */
	public String getApiKey();
		
    /**
     * Get the URL for Galaxy workflow Rest API.
     * @return
     */
    public String getWorkflowUrl();
	
	/**
	 * Retrieve all currently existing workflows from Galaxy through its REST API.
	 * @return
	 */
	public GalaxyWorkflow[] getWorkflows();

}
