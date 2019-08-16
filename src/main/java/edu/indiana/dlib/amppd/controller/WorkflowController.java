package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.service.GalaxyApiService;
import lombok.extern.java.Log;

/**
 * Controller for REST operations on Workflow.
 * @author yingfeng
 *
 */
@RestController
@Log
public class WorkflowController {
	
	@Autowired
	private GalaxyApiService galaxyApiService;

	/**
	 * Retrieve all workflows from Galaxy through its REST API.
	 * @return
	 */
	@GetMapping("/workflows")
	public List<Workflow> getWorkflows() {	
		List<Workflow> workflows = null;
	
		try {
			workflows = galaxyApiService.getGalaxyInstance().getWorkflowsClient().getWorkflows();
			log.info("Retrieved " + workflows.size() + " current workflows in Galaxy: " + workflows);
		}
		catch (Exception e) {
			String msg = "Unable to retrieve workflows from Galaxy instance.";
			log.severe(msg);
			throw new RuntimeException(msg, e);
		}
		
		return workflows;
	}
	
	/**
	 * Run the given workflow against the given primaryfile.
	 * @return outputs of the job run
	 */
	@PostMapping("/workflow/{workflowId}/primaryfile/{primaryfileId}")
	public WorkflowOutputs runWorkflow(@PathVariable("workflowId") Long workflowId, @RequestParam("primaryfileId") Long primaryfileId) {
		
	}

}
