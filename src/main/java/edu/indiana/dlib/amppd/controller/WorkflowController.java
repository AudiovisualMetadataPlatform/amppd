package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import edu.indiana.dlib.amppd.model.GalaxyWorkflow;
import lombok.extern.java.Log;

/**
 * Controller for REST operations on Workflow.
 * @author yingfeng
 *
 */
@RestController
@Log
public class WorkflowController {
	
    private RestTemplate restTemplate = new RestTemplate();
	
	@Value("${galaxy.workflow.url:http://localhost:8300/api/workflows}")
	private String workflowUrl;
	
	/**
	 * Helper method to return the base URL of Galaxy REST service.
	 * @return
	 */
	public String getWorkflowUrl() {
		if (workflowUrl == null) workflowUrl = "http://localhost:8080/api/workflows"; // TODO remove this line, which is a work-around for @Value not taking effect
		return workflowUrl;
	}
	
	/**
	 * Helper method to retrieve the API key for the current user from Galaxy. The key is used as a token for every REST request made to Galaxy.
	 * @return
	 */
	public String getApiKey() {
		String key = "ffe172319385ae7644a65bc59d5052dc";
		/* TODO get api key with following 
		request: curl –user zipzap@foo.com:password http://localhost:8080/api/authenticate/baseauth
		response: {“api_key”: “baa4d6e3a156d3033f05736255f195f9” }
		 */
		return key;
	}
	
	/**
	 * Retrieve all workflows from Galaxy through its REST API.
	 * @return
	 */
	@GetMapping("/workflows")
	public GalaxyWorkflow[] getWorkflows() {		
		String url = getWorkflowUrl() + "?=" + getApiKey();
		GalaxyWorkflow[] workflows = restTemplate.getForObject(url, GalaxyWorkflow[].class);
		log.info("Current workflows in Galaxy: " + workflows);
		return workflows;
	}

}
