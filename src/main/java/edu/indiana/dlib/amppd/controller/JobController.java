package edu.indiana.dlib.amppd.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationBriefs;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationDetails;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationStepDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.web.WorkflowOutputResult;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on Amppd jobs.
 * @author yingfeng
 *
 */
@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class JobController {	

	@Autowired
	private GalaxyPropertyConfig galaxyPropertyConfig;
	
	@Autowired
	private JobService jobService;
	
	/**
	 * Creating an Amppd job to invoke the given workflow in Galaxy against the given primaryfile with the given step parameters.
	 * @param workflowId the ID of the workflow 
	 * @param primaryfileId the ID of the primaryfile
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return WorkflowOutputResult containing detailed information for the workflow submission on the primaryfile
	 */
	@PostMapping("/jobs/submitFile")
	public WorkflowOutputResult createJob(
			@RequestParam("workflowId") String workflowId, 
			@RequestParam("primaryfileId") Long primaryfileId, 
			@RequestParam(value = "parameters", required = false) Map<String, Map<String, String>> parameters) {	
		if (parameters == null ) {
			parameters = new HashMap<String, Map<String, String>>();
		}
		log.info("Creating Amppd job for: workflowId: " + workflowId + ", primaryfileId: " + primaryfileId + " parameters: " + parameters);
		return jobService.createJob(workflowId, primaryfileId, parameters);
	}

	/**
	 * Create new Amppd jobs by submitting to Galaxy the given workflow on the given primaryfiles, along with the given parameters.
	 * @param workflowId ID of the given workflow
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return list of WorkflowOutputResult containing detailed information for the workflow submission on the primaryfile
	 */
	@PostMapping("/jobs/submitFiles")
	public List<WorkflowOutputResult> createJobs(			
			@RequestParam("workflowId") String workflowId, 
			@RequestParam("primaryfileIds") Long[] primaryfileIds, 
			@RequestParam(value = "parameters", required = false) Map<String, Map<String, String>> parameters) {	
		if (parameters == null ) {
			parameters = new HashMap<String, Map<String, String>>();
		}
		log.info("Creating Amppd jobs for: workflowId: " + workflowId + ", primaryfileIds: " + primaryfileIds + " parameters: " + parameters);
		return jobService.createJobs(workflowId, primaryfileIds, parameters);
	}
	
	/**
	 * Creating a bundle of multiple Amppd jobs, one for each primaryfile included in the given bundle, to invoke the given workflow in Galaxy, with the given step parameters.
	 * @param workflowId the ID of the specified workflow 
	 * @param bundleId the ID of the specified bundle
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return list of WorkflowOutputResult containing detailed information for the workflow submission on the primaryfile
	 */
	@CrossOrigin(origins = "*")
	@PostMapping("/jobs/submitBundle")
	public List<WorkflowOutputResult> createJobBundle(
			@RequestParam("workflowId") String workflowId, 
			@RequestParam("bundleId") Long bundleId, 
			@RequestParam(value = "parameters", required = false) Map<String, Map<String, String>> parameters) {	
		// if parameters is not specified in the request, use an empty map for it
		if (parameters == null ) {
			parameters = new HashMap<String, Map<String, String>>();
		}
		log.info("Creating a bundle of Amppd jobs for: workflowId: " + workflowId + ", bundleId: " + bundleId + " parameters: " + parameters);		
		return jobService.createJobBundle(workflowId, bundleId, parameters);
	}

	/**
	 * List all AMP jobs with step details, i.e. all workflow invocations submitted via AMPPD UI to Galaxy.
	 * @return a list of Invocations each containing basic information of an AMP job.
	 */
	@GetMapping("/jobs/details")
	public List<InvocationDetails> listJobsDetails() {
		log.info("Listing all AMP jobs with step details: ");		
		/* Note: 
		 * Galaxy admin can list invocations owned by any user; while non-admin can only list self-owned ones.
		 * Since all invocations are submitted as amppd master user, which is a Galaxy admin, its username
		 * is passed as the query parameter, and the returned list will include all AMPPD jobs.
		 */
		return jobService.getWorkflowsClient().indexInvocationsDetails(galaxyPropertyConfig.getUsername());
	}
	
	/**
	 * List all AMP jobs run on the specified workflow against the specified primaryfile.
	 * @return a list of Invocations each containing basic information of an AMP job.
	 */
	@GetMapping("/jobs")
	public List<Invocation> listJobs(
			@RequestParam("workflowId") String workflowId, 
			@RequestParam("primaryfileId") Long primaryfileId) {
		log.info("Listing all AMP jobs for: workflowId: " + workflowId + ", primaryfileId: " + primaryfileId);		
		return jobService.listJobs(workflowId, primaryfileId);
	}
	
	/**
	 * Show brief information of the inquired AMP job, which includes basic information of each step within the job. 
	 * @param workflowId the ID of the workflow associated with the inquired AMP job 
	 * @param invocationId the ID of the Galaxy workflow invocation corresponding to the inquired AMP job
	 * @return an instance of InvocationBriefs containing brief information of the inquired AMP job 
	 */
	@GetMapping("/jobs/{invocationId}")
	public InvocationBriefs showJob(
			@RequestParam("workflowId") String workflowId, 
			@PathVariable("invocationId") String invocationId) {
		log.info("Showing information of the AMP job for: workflowId: " + workflowId + ", invocationId: " + invocationId);		
		return (InvocationBriefs)jobService.getWorkflowsClient().showInvocation(workflowId, invocationId, false);
	}
	
	/**
	 * Show detailed information of the inquired step within the specified AMP job. 
	 * @param workflowId the ID of the workflow associated with the specified AMP job 
	 * @param invocationId the ID of the Galaxy workflow invocation corresponding to the AMP job
	 * @param stepId the ID of the inquired Galaxy workflow invocation step within the AMP job
	 * @return an instance of InvocationStepDetails containing detailed information of the inquired AMP job step
	 */
	@GetMapping("/jobs/{invocationId}/steps/{stepId}")
	public InvocationStepDetails showJobStep(
			@RequestParam("workflowId") String workflowId, 
			@PathVariable("invocationId") String invocationId,
			@PathVariable("stepId") String stepId) {
		log.info("Showing information of the AMP job step for: workflowId: " + workflowId + ", invocationId: " + invocationId + ", stepId: " + stepId);		
		return jobService.getWorkflowsClient().showInvocationStep(workflowId, invocationId, stepId);
	}
	
	/**
	 * Show detailed information of the inquired output generated by the specified AMP job step. 
	 * @param workflowId the ID of the workflow associated with the specified AMP job 
	 * @param invocationId the ID of the Galaxy workflow invocation corresponding to the specified AMP job
	 * @param stepId the ID of the specified Galaxy workflow invocation step within the AMP job
	 * @param datasetId the ID of the inquired Galaxy workflow invocation step output dataset
	 * @return an instance of Dataset containing detailed information of the inquired AMP job step output
	 */
	@GetMapping("/jobs/{invocationId}/steps/{stepId}/outputs/{datasetId}")
	public Dataset showJobStepOutput(
			@RequestParam("workflowId") String workflowId, 
			@PathVariable("invocationId") String invocationId,
			@PathVariable("stepId") String stepId,
			@PathVariable("datasetId") String datasetId) {
		log.info("Showing information of the AMP job step output for: workflowId: " + workflowId + ", invocationId: " + invocationId + ", stepId: " + stepId + ", datasetId: " + datasetId);		
		return (Dataset)jobService.showJobStepOutput(workflowId, invocationId, stepId, datasetId);
	}
	
}
