package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.service.WorkflowService;
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
	private WorkflowService workflowService;
	
	/**
	 * List all workflows currently existing in Galaxy.
	 * @return a list of workflows with name, ID, and URL.
	 */
	@GetMapping("/workflows")
	public List<Workflow> listWorkflows() {	
		List<Workflow> workflows = null;
	
		try {
			workflows = workflowService.getWorkflowsClient().getWorkflows();
			log.info("Listing " + workflows.size() + " workflows currently existing in Galaxy: " + workflows);
		}
		catch (Exception e) {
			String msg = "Unable to retrieve workflows from Galaxy.";
			log.severe(msg);
			throw new GalaxyWorkflowException(msg, e);
		}
		
		return workflows;
	}
	
	/**
	 * Show details of a workflow based on information retrieved from Galaxy.
	 * @param workflowId ID of the queried workflow
	 * @return all the details information of the queried workflow
	 */
	@GetMapping("/workflows/{workflowId}")
	public WorkflowDetails showWorkflow(@PathVariable("workflowId") String workflowId) {	
		WorkflowDetails workflow = null;
	
		try {
			workflow = workflowService.getWorkflowsClient().showWorkflow(workflowId);
			log.info("Showing workflow detail for ID: " +  workflowId);
		}
		catch (Exception e) {
			String msg = "Unable to retrieve workflow detail for ID " + workflowId + " from Galaxy.";
			log.severe(msg);
			throw new GalaxyWorkflowException(msg, e);
		}

		return workflow;
	}
	
	
}
