package edu.indiana.dlib.amppd.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.web.WorkflowOutputResult;


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
	 * Return the HistoriesClient instance.
	 */
	public HistoriesClient getHistoriesClient();
	
	/**
	 * Get needed job context for HMGMs when running the given workflow against the given primaryfile.
	 * @param workflowDetails the given workflow
	 * @param primaryfile the given primaryfile
	 * @return the generated JSON string for HMGM context
	 */
	public String getHmgmContext(WorkflowDetails workflowDetails, Primaryfile primaryfile); 
	
	/**
	 * Sanitize the given text by replacing single/double quotes (if any) with "%" followed by their hex code,
	 * so the result can be used in a context JSON string, which can be parsed as a valid parameter on command line.
	 * @param text text to be sanitized
	 * @return
	 */
	public String sanitizeText(String text);
	
//	/**
//	 * Create a new Amppd job to invoke the given workflow in Galaxy on the given primaryfile and previous outputs, 
//	 * along with the given parameters.
//	 * @param workflowId ID of the given workflow
//	 * @param primaryfileId ID of the given primaryfile; if not null, then the primaryfile must be the first workflow input; 
//	 * 			otherwise no primaryfile is used as input.
//	 * @param outputIds array of IDs of the given outputs; if null then no output is used as workflow input
//	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
//	 * @return WorkflowOutputResult containing detailed information for the file submitted
//	 */
//	public WorkflowOutputResult createJob(String workflowId, Long primaryfileId, String[] outputIds, Map<String, Map<String, String>> parameters);

	/**
	 * Create a new Amppd job to invoke the given workflow in Galaxy on the given primaryfile and previous WorkflowResult outputs, 
	 * along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param primaryfileId ID of the given primaryfile; if not null, then the primaryfile must be the first workflow input; 
	 * 			otherwise no primaryfile is used as input.
	 * @param resultIds array of WorkflowResult IDs of the given outputs; if null then no output is used as workflow input
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return WorkflowOutputResult containing detailed information for the job submitted
	 */
	public WorkflowOutputResult createJob(String workflowId, Long primaryfileId, Long[] resultIds, Map<String, Map<String, String>> parameters);

	/**
	 * Create a new Amppd job to invoke the given workflow in Galaxy on the given primaryfile, along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param primaryfileId ID of the given primaryfile
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return WorkflowOutputResult containing detailed information for the job submitted
	 */
	public WorkflowOutputResult createJob(String workflowId, Long primaryfileId, Map<String, Map<String, String>> parameters);
	
	/**
	 * Create a new Amppd job to invoke the given workflow in Galaxy on the given previous outputs, along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param resultIds array of WorkflowResult IDs of the given outputs
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return WorkflowOutputResult containing detailed information for the job submitted
	 */
	public WorkflowOutputResult createJob(String workflowId, Long[] resultIds, Map<String, Map<String, String>> parameters);

	/**
	 * Create new Amppd jobs to invoke the given workflow in Galaxy on the given primaryfiles, along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return WorkflowOutputResult containing detailed information for the job submitted
	 */
	public List<WorkflowOutputResult> createJobs(String workflowId, Long[] primaryfileIds, Map<String, Map<String, String>> parameters);
	
	/**
	 * Create a bundle of multiple Amppd jobs, one job for each primaryfile included in the given bundle, to invoke the given workflow in Galaxy, with the given step parameters.
	 * @param workflowId the ID of the specified workflow 
	 * @param bundleId the ID of the specified bundle
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return list of WorkflowOutputResult containing detailed information for the job submitted
	 */
	public List<WorkflowOutputResult> createJobBundle(String workflowId, Long bundleId, Map<String, Map<String, String>> parameters);
	
	/**
	 * Create Amppd jobs, one for each row of primaryfile and outputs specified in the given inputCsv, 
	 * to invoke the given workflow in Galaxy along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param inputCsv CSV file each row specifying the primaryfile and previous outputs to use as workflow inputs
	 * @param includePrimaryfile if true include the primaryfile as the first input for each job; otherwise ignore the primaryfile
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return WorkflowOutputResult containing detailed information for the job submitted
	 */
	public List<WorkflowOutputResult> createJobs(String workflowId, MultipartFile inputCsv, Boolean includePrimaryfile, Map<String, Map<String, String>> parameters);

	/**
	 * List all AMP jobs run on the specified workflow against the specified primaryfile.
	 * @param workflowId ID of the given workflow
	 * @param(primaryfileId ID of the given primaryfile 
	 * @return a list of Invocations each containing basic information of an AMP job 
	 */
	public List<Invocation> listJobs(String workflowId, Long primaryfileId);
		
	/**
	 * Show detailed information of the inquired output generated by the specified AMP job step. 
	 * @param workflowId the ID of the workflow associated with the specified AMP job 
	 * @param invocationId the ID of the Galaxy workflow invocation corresponding to the specified AMP job
	 * @param stepId the ID of the specified Galaxy workflow invocation step within the AMP job
	 * @param datasetId the ID of the inquired Galaxy workflow invocation step output dataset
	 * @return an instance of Dataset containing detailed information of the inquired AMP job step output
	 */
	public Dataset showJobStepOutput(String workflowId, String invocationId, String stepId, String datasetId);
		

}
