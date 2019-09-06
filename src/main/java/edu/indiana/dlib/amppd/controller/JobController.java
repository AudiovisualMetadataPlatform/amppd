package edu.indiana.dlib.amppd.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.service.JobService;
import lombok.extern.java.Log;

/**
 * Controller for REST operations on Amppd jobs.
 * @author yingfeng
 *
 */
@RestController
@Log
public class JobController {	

	@Autowired
	private JobService jobService;
	
	/**
	 * Creating an Amppd job to invoke the given workflow in Galaxy against the given primaryfile with the given step parameters.
	 * @param workflowId the ID of the workflow 
	 * @param primaryfileId the ID of the primaryfile
	 * @param parameters the parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return outputs of the invocation returned by Galaxy
	 */
	@PostMapping("/jobs")
	public WorkflowOutputs createJob(
			@RequestParam("workflowId") String workflowId, 
			@RequestParam("primaryfileId") Long primaryfileId, 
			@RequestParam("parameters") Map<String, Map<String, String>> parameters) {	
		log.info("Creating Amppd job for: workflow ID: " + workflowId + ", primaryfileId: " + primaryfileId + " parameters: " + parameters);
		return jobService.createJob(workflowId, primaryfileId, parameters);
	}

	/**
	 * Creating multiple Amppd jobs, one for each primaryfile of the items included in the given bundle, to invoke the given workflow in Galaxy, with the given step parameters.
	 * @param workflowId the ID of the specified workflow 
	 * @param bundleId the ID of the specified bundle
	 * @param parameters the parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @return list outputs of the invocation returned by Galaxy
	 */
	@PostMapping("/jobs/bundle")
	public List<WorkflowOutputs> createJobBundle(
			@RequestParam("workflowId") String workflowId, 
			@RequestParam("bundleId") Long bundleId, 
			@RequestParam("parameters") Map<String, Map<String, String>> parameters) {	
		log.info("Creating a bundle Amppd jobs for: workflow ID: " + workflowId + ", bundleId: " + bundleId + " parameters: " + parameters);		
		return jobService.createJobBundle(workflowId, bundleId, parameters);
	}

}
