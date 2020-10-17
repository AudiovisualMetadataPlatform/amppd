package edu.indiana.dlib.amppd.service;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

/**
 * Service for workflow related functionalities.
 * @author yingfeng
 *
 */
public interface WorkflowService {

	/**
	 * Get the WorkflowsClient instance.
	 */
	public WorkflowsClient getWorkflowsClient();
	
	/**
	 * Get the workflow with the specified name.
	 * @param workflowName the name of the specified workflow
	 * @return the workflow requested
	 */
	public Workflow getWorkflow(String workflowName);
	
	/**
	 * Get the name of the specified workflow and store it in a local cache: 
	 * first, search in the local cache;
	 * if not found. query Galaxy;
	 * if still not found, use the ID as the name.
	 * @param workflowId the ID of the specified workflow
	 * @return the workflow name
	 */
	public String getWorkflowName(String workflowId);
	
}
