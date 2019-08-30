package edu.indiana.dlib.amppd.service;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;

/**
 * Service for workflow related functionalities.
 * @author yingfeng
 *
 */
public interface WorkflowService {

	/**
	 * Return the WorkflowsClient instance.
	 */
	public WorkflowsClient getWorkflowsClient();
	
}
