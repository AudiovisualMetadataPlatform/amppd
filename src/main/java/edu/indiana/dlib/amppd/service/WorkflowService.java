package edu.indiana.dlib.amppd.service;

import java.util.List;
import java.util.Map;

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
	 * Returns a list of input IDs for the given workflow, or null if the workflow doesn't exist.
	 * Note: This method is an bugfix/extension to the blend4j WorkflowsClientImpl.showWorkflow, which unfortunately always returns empty list for the WorkflowDetails.inputs.
	 * @param workflowId ID of the given workflow
	 * @return a list of IDs of the workflow inputs
	 */
	public List<String> getWorkflowInputs(String workflowId);
	
	/**
	 * Build the workflow inputs to feed the given dataset along with the given parameters into the given  Galaxy workflow.
	 * @param workflowId ID of the given workflow
	 * @param datasetId ID of the given dataset
	 * @param parameters step parameters for running the workflow
	 * @return the built workflow inputs
	 */
	public WorkflowInputs buildWorkflowInputs(String workflowId, String datasetId, Map<String, Map<String, String>> parameters);
	
	
}
