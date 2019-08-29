package edu.indiana.dlib.amppd.service;

import java.util.Map;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;


/**
 * Service for job related functionalities.
 * @author yingfeng
 *
 */
public interface JobService {

	/**
	 * Return the WorkflowsClient instance.
	 */
	public WorkflowsClient getWorkflowsClient();
	
	/**
	 * Build the workflow inputs to feed the given dataset along with the given parameters into the given  Galaxy workflow.
	 * @param workflowId ID of the given workflow
	 * @param datasetId ID of the given dataset
	 * @param parameters step parameters for running the workflow
	 * @return the built workflow inputs
	 */
	public WorkflowInputs buildWorkflowInputs(String workflowId, String datasetId, Map<String, Map<String, String>> parameters);
	
}
