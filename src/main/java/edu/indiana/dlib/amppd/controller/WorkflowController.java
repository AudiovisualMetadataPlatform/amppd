package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.service.WorkflowService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on Workflow.
 * @author yingfeng
 *
 */
@CrossOrigin(origins = "*")
@RestController
@Slf4j
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
			log.error(msg);
			throw new GalaxyWorkflowException(msg, e);
		}
		
		return workflows;
	}
	
	/**
	 * Show details of a workflow based on information retrieved from Galaxy.
	 * Note: Set instance to true if the workflow ID is returned from invocation listing, in which case it's likely not a StoredWorkflow ID.
	 * @param workflowId ID of the queried workflow
	 * @param instance true if fetch by Workflow ID instead of StoredWorkflow id, false by default
	 * @return all the details information of the queried workflow
	 */
	@GetMapping("/workflows/{workflowId}")
	public WorkflowDetails showWorkflow(@PathVariable("workflowId") String workflowId, @RequestParam(name = "instance", required = false) Boolean instance) {	
		WorkflowDetails workflow = null;
	
		try {
			if (instance != null && instance) {
				workflow = workflowService.getWorkflowsClient().showWorkflow(workflowId);
				log.info("Showing stored workflow detail for ID: " +  workflowId);
			}
			else {
				workflow = workflowService.getWorkflowsClient().showWorkflowInstance(workflowId);
				log.info("Showing possibly non-stored workflow instance detail for ID: " +  workflowId);				
			}
		}
		catch (Exception e) {
			String msg = "Unable to retrieve workflow detail for ID " + workflowId + " from Galaxy.";
			log.error(msg);
			throw new GalaxyWorkflowException(msg, e);
		}

		return workflow;
	}
	
	
}
