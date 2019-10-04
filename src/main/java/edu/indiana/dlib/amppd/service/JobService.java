package edu.indiana.dlib.amppd.service;

import java.util.List;
import java.util.Map;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;


/**
 * Service for Amppd job related functionalities. 
 * Note: unless otherwise noted, all references of job in Amppd refer to Amppd jobs. An Amppd job is an execution of a Galaxy workflow on a Amppd primaryfile.
 * @author yingfeng
 *
 */
public interface JobService {

	/**
	 * Return the WorkflowsClient instance.
	 */
	public WorkflowsClient getWorkflowsClient();
	
	/**
	 * Build the workflow inputs to feed the given dataset and history along with the given parameters into the given Galaxy workflow.
	 * @param workflowId ID of the given workflow
	 * @param datasetId ID of the given dataset
	 * @param historyId ID of the given history
	 * @param parameters step parameters for running the workflow
	 * @return the built WorkflowInputs instance
	 */
	public WorkflowInputs buildWorkflowInputs(String workflowId, String datasetId, String historyId, Map<String, Map<String, String>> parameters);
	
	/**
	 * Create a new Amppd job by submitting to Galaxy the given workflow on the given primaryfile, along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param primaryfileId ID of the given primaryfile
	 * @param parameters step parameters for running the workflow
	 * @return the WorkflowOutputs returned from Galaxy
	 */
	public WorkflowOutputs createJob(String workflowId, Long primaryfileId, Map<String, Map<String, String>> parameters);
	
	/**
	 * Create a bundle of multiple Amppd jobs, one job for each primaryfile of the items included in the given bundle, to invoke the given workflow in Galaxy, with the given step parameters.
	 * @param workflowId the ID of the specified workflow 
	 * @param bundleId the ID of the specified bundle
	 * @param parameters the parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return list outputs of the invocation returned by Galaxy
	 */
	public List<WorkflowOutputs> createJobBundle(String workflowId, Long bundleId, Map<String, Map<String, String>> parameters);
	
	/**
	 * List all AMP jobs run on the specified workflow against the specified primaryfile.
	 * @param workflowId ID of the given workflow
	 * @param(primaryfileId ID of the given primaryfile 
	 * @return a list of Invocations each containing basic information of an AMP job 
	 */
	public List<Invocation> listJobs(String workflowId, Long primaryfileId);
		

}
