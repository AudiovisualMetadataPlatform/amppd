package edu.indiana.dlib.amppd.service;

import java.util.Map;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;


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
	 * Build the workflow inputs to feed the given dataset along with the given parameters into the given Galaxy workflow.
	 * @param workflowId ID of the given workflow
	 * @param datasetId ID of the given dataset
	 * @param parameters step parameters for running the workflow
	 * @return the built WorkflowInputs instance
	 */
	public WorkflowInputs buildWorkflowInputs(String workflowId, String datasetId, Map<String, Map<String, String>> parameters);
	
	/**
	 * Create a new Amppd job by submitting to Galaxy the given workflow on the given primaryfile, along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param primaryfileId ID of the given primaryfile
	 * @param parameters step parameters for running the workflow
	 * @return the WorkflowOutputs returned from Galaxy
	 */
	public WorkflowOutputs createJob(String workflowId, Long primaryfileId, Map<String, Map<String, String>> parameters);
	

}
