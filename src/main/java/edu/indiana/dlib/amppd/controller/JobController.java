package edu.indiana.dlib.amppd.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationBriefs;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationDetails;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationStepDetails;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.web.CreateJobParameters;
import edu.indiana.dlib.amppd.web.CreateJobResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on AMP jobs.
 * @author yingfeng
 */
//@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class JobController {	

	@Autowired
	private GalaxyPropertyConfig galaxyPropertyConfig;
	
	@Autowired
	private JobService jobService;
	

	/**
	 * Create AMP jobs by submitting to Galaxy the given workflow against the given primaryfiles, along with the given parameterss.
	 * @param workflowId ID of the given workflow
	 * @param primaryfileIds IDs of the given primaryfiles
	 * @param parameterss the dynamic parameterss to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return CreateJobResponse containing detailed information for the workflow submission on the inputs
	 */
	@PostMapping(path = "/jobs/submitFiles", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<CreateJobResponse> createJobs(			
			@RequestParam String workflowId, 
			@RequestParam Long[] primaryfileIds, 
			@RequestBody(required = false) CreateJobParameters[] parameterss) {		
		log.info("Processing request to submit a workflow against primaryfiles with parameterss ... ");
		return jobService.createJobs(workflowId, primaryfileIds, parameterss);
	}
	
	/**
	 * Create a bundle of AMP jobs, one for each primaryfile included in the given bundle, by submitting to Galaxy the given workflow with the given step parameterss.
	 * @param workflowId the ID of the specified workflow 
	 * @param bundleId the ID of the specified bundle
	 * @param parameterss the dynamic parameterss to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return CreateJobResponse containing detailed information for the workflow submission on the inputs
	 */
	@PostMapping(path = "/jobs/submitBundle", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<CreateJobResponse> createJobs(
			@RequestParam String workflowId, 
			@RequestParam Long bundleId, 
			@RequestBody(required = false) CreateJobParameters[] parameterss) {	
		log.info("Processing request to submit a workflow against a bundle of primaryfiles with parameterss ... ");
		return jobService.createJobs(workflowId, bundleId, parameterss);
	}

	/**
	 * Create AMP jobs, one for each row of WorkflowResult outputs specified in the given list of arrays, to invoke the given workflow in
	 * Galaxy along with the given parameterss, including their associated primaryfile as the first input if the given indicator is true. 
	 * @param workflowId ID of the given workflow
	 * @param resultIdss list of arrays of WorkflowResult IDs of the given outputs
	 * @param parameterss the dynamic parameterss to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @param includePrimaryfile if true include the primaryfile as the first input for each job
	 * @return list of CreateJobResponses containing detailed information for the job submitted
	 */
	@PostMapping(path = "/jobs/submitResults", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<CreateJobResponse> createJobs(
			@RequestParam String workflowId, 
			@RequestParam List<Long[]> resultIdss, 
			@RequestBody(required = false) CreateJobParameters[] parameterss,
			@RequestParam(required = false) Boolean includePrimaryfile) {
		if (resultIdss == null ) {
			resultIdss = new ArrayList<Long[]>();
		}
		else if (!resultIdss.isEmpty() && resultIdss.get(resultIdss.size()-1).length == 0) {
			// with resultIdss[m][n] (m=1, n>1), the request parser tends to convert the parameter into n lists of size 1; 
			// to avoid this, one can add an empty array to the end of the parameter to clarify the dimension,
			// in which case, we need to remove the last empty array to avoid error
			resultIdss.remove(resultIdss.size()-1);
		}
		if (includePrimaryfile == null ) {
			includePrimaryfile = false;
		}		
		log.info("Processing request to submit a workflow against a list of arrays of workflow result outputs with parameterss ... ");
		return jobService.createJobs(workflowId, resultIdss, parameterss, includePrimaryfile);
	}

	/**
	 * Create AMP jobs, one for each row of primaryfile and outputs specified in the given csvFile, 
	 * by submitting to Galaxy the given workflow along with the given parameterss.
	 * @param workflowId ID of the given workflow
	 * @param inputCsv CSV file each row specifying the primaryfile and previous outputs to use as workflow inputs
	 * @param includePrimaryfile if true include the primaryfile as the first input for each job; otherwise (default false) ignore the primaryfile
	 * @param parameterss the dynamic parameterss to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return CreateJobResponse containing detailed information for the workflow submission on the inputs
	 */
	@PostMapping(path = "/jobs/submitCsv", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<CreateJobResponse> createJobs(
			@RequestParam String workflowId, 
			@RequestParam MultipartFile inputCsv,
			@RequestParam(value = "parameterss", required = false) CreateJobParameters[] parameterss,
			@RequestParam(required = false) Boolean includePrimaryfile) {
		// TODO 
		// parameterss is supposed to use @RequestBody or @RequestPart in order to be parsed as JSON string properly;
		// but for some reason Spring Boot request handler throws exception on multipart request boundary;
		// as a workaround, @RequestParam is used for now
		if (includePrimaryfile == null ) {
			includePrimaryfile = false;
		}
		log.info("Processing request to submit a workflow against an inputCsv file containing primaryfile IDs and workflow result IDs with parameterss ... ");
		return jobService.createJobs(workflowId, inputCsv, parameterss, includePrimaryfile);
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
			@RequestParam String workflowId, 
			@RequestParam Long primaryfileId) {
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
			@RequestParam String workflowId, 
			@PathVariable String invocationId) {
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
			@RequestParam String workflowId, 
			@PathVariable String invocationId,
			@PathVariable String stepId) {
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
			@RequestParam String workflowId, 
			@PathVariable String invocationId,
			@PathVariable String stepId,
			@PathVariable String datasetId) {
		log.info("Showing information of the AMP job step output for: workflowId: " + workflowId + ", invocationId: " + invocationId + ", stepId: " + stepId + ", datasetId: " + datasetId);		
		return (Dataset)jobService.showJobStepOutput(workflowId, invocationId, stepId, datasetId);
	}
	
}
