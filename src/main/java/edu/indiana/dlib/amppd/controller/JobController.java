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
 * Controller for REST operations on Workflow.
 * @author yingfeng
 *
 */
@RestController
@Log
public class JobController {	

	@Autowired
	private JobService jobService;
	
	/**
	 * Run the given workflow against the given primaryfile.
	 * @return outputs of the job run
	 */
	@PostMapping("/jobs/")
	public WorkflowOutputs runWorkflow(
			@RequestParam("workflowId") String workflowId, 
			@RequestParam("primaryfileId") Long primaryfileId, 
			@RequestParam("parameters") Map<String, Map<String, String>> parameters) {		
		return jobService.createJob(workflowId, primaryfileId, parameters);
	}

}
