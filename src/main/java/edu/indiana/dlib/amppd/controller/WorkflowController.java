package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.model.galaxy.GalaxyWorkflow;
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
	public GalaxyWorkflow[] getWorkflows() {		
		return galaxyApiService.getWorkflows();
	}

}
