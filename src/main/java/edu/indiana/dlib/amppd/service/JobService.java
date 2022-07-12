package edu.indiana.dlib.amppd.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.web.CreateJobParameters;
import edu.indiana.dlib.amppd.web.CreateJobResponse;


/**
 * Service for AMP job related functionalities. 
 * Note: unless otherwise noted, all references of job in Amppd refer to AMP jobs. An AMP job is an execution of a Galaxy workflow on a Amppd primaryfile.
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
	
	/**
	 * Parse the given input CSF file into a list of arrays of WorkflowResult IDs.
	 * @param inputCsv the given input CSF file
	 * @return resultIdss the generated list of arrays of WorkflowResult IDs
	 */
	public List<Long[]> parseInputCsv(MultipartFile inputCsv);		
	
	/**
	 * Create an AMP job to invoke the given workflow in Galaxy on the given primaryfile, along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param primaryfileId ID of the given primaryfile
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return CreateJobResponse containing detailed information for the job submitted
	 */
	public CreateJobResponse createJob(WorkflowDetails workflowDetails, Long primaryfileId, Map<String, Map<String, String>> parameters);
	
	/**
	 * Create an AMP job to invoke the given workflow in Galaxy on the given previous WorkflowResult outputs, 
	 * along with the given parameters, including the associated primaryfile as the first input if the given indicator is true. 
	 * @param workflowId ID of the given workflow
	 * @param resultIds array of WorkflowResult IDs of the given outputs
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @param includePrimaryfile if true include the primaryfile as the first input
	 * @return CreateJobResponse containing detailed information for the job submitted
	 */
	public CreateJobResponse createJob(WorkflowDetails workflowDetails, Long[] resultIds, Map<String, Map<String, String>> parameters, Boolean includePrimaryfile);

	/**
	 * Create AMP jobs to invoke the given workflow in Galaxy on the given primaryfiles, along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return list of WorkflowOutputResults containing detailed information for the job submitted
	 */
	public List<CreateJobResponse> createJobs(String workflowId, Long[] primaryfileIds, CreateJobParameters[] parameterss);
	
	/**
	 * Create a bundle of multiple AMP jobs, one job for each primaryfile included in the given bundle, to invoke the given workflow in Galaxy, with the given step parameters.
	 * @param workflowId the ID of the specified workflow 
	 * @param bundleId the ID of the specified bundle
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return list of WorkflowOutputResults containing detailed information for the job submitted
	 */
	public List<CreateJobResponse> createJobs(String workflowId, Long bundleId, CreateJobParameters[] parameterss);
	
	/**
	 * Create AMP jobs, one for each row of WorkflowResult outputs specified in the given list of arrays, to invoke the given workflow in
	 * Galaxy along with the given parameters, including their associated primaryfile as the first input if the given indicator is true. 
	 * @param workflowId ID of the given workflow
	 * @param resultIdss list of arrays of WorkflowResult IDs of the given outputs
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @param includePrimaryfile if true include the primaryfile as the first input for each job
	 * @return list of WorkflowOutputResults containing detailed information for the job submitted
	 */
	public List<CreateJobResponse> createJobs(String workflowId, List<Long[]> resultIdss, CreateJobParameters[] parameterss, Boolean includePrimaryfile);

	/**
	 * Create AMP jobs, one for each row of primaryfile and outputs specified in the given inputCsv, to invoke the given workflow in
	 * Galaxy along with the given parameters, including their associated primaryfile as the first input if the given indicator is true. 
	 * @param workflowId ID of the given workflow
	 * @param inputCsv CSV file each row specifying the primaryfile and previous outputs to use as workflow inputs
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @param includePrimaryfile if true include the primaryfile as the first input for each job
	 * @return list of WorkflowOutputResults containing detailed information for the job submitted
	 */
	public List<CreateJobResponse> createJobs(String workflowId, MultipartFile inputCsv, CreateJobParameters[] parameterss, Boolean includePrimaryfile);

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
