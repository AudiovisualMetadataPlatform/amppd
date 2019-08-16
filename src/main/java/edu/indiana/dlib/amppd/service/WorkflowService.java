package edu.indiana.dlib.amppd.service;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;

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
	
	/**
	 * Build the workflow inputs to feed the given dataset into the given  Galaxy workflow.
	 * @param workflowId ID of the given workflow
	 * @param datasetId ID of the given dataset
	 */
	public WorkflowInputs buildWorkflowInputs(String workflowId, String datasetId);
	
	
}
