package edu.indiana.dlib.amppd.controller;

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
	 * Creating an Amppd job to invoke the given workflow in Galaxy against the given primaryfile with the given parameters.
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

}
