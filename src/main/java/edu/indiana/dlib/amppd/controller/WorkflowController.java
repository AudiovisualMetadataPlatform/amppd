package edu.indiana.dlib.amppd.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import edu.indiana.dlib.amppd.web.WorkflowResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on Workflow.
 * @author yingfeng
 */
//@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class WorkflowController {

	@Autowired
	private WorkflowService workflowService;

	@Autowired
	private PermissionService permissionService;
	
	
	/**
	 * List all workflows currently existing in Galaxy.
	 * @return a list of workflows with name, ID, and URL.
	 */
	@GetMapping("/workflows")
	public WorkflowResponse listWorkflows(
			@RequestParam(required = false) Boolean showPublished,
			@RequestParam(required = false) Boolean showHidden,
			@RequestParam(required = false) Boolean showDeleted,
			@RequestParam(required = false) String[] tags,
			@RequestParam(required = false) String[] name,
			@RequestParam(required = false) String[] annotations,
			@RequestParam(required = false) String[] creator,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date[] dateRange) {
		// check permission 
		// Note: Since workflow is not associated with any unit, the AC is checked against any unit
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Workflow, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot view workflow in any unit.");
		}
		
		try {
			String published = showPublished == null ? "null" : showPublished.toString();
			String hidden = showHidden == null ? "null" : showHidden.toString();
			String deleted = showDeleted == null ? "null" : showDeleted.toString();
			log.info("Listing workflows in Galaxy, showPublished = " + published + ", showHidden = " + hidden + ", showDeleted = " + deleted);
			return workflowService.listWorkflows(showPublished, showHidden, showDeleted, tags, name, annotations, creator, dateRange);
		}
		catch (Exception e) {
			String msg = "Unable to retrieve workflows from Galaxy.";
			log.error(msg);
			throw new GalaxyWorkflowException(msg, e);
		}
	}
	
	/**
	 * Show details of a workflow based on information retrieved from Galaxy.
	 * Note: Set instance to true if the workflow ID is returned from invocation listing, in which case it's likely not a StoredWorkflow ID.
	 * @param workflowId ID of the queried workflow
	 * @param instance true if fetch by Workflow ID instead of StoredWorkflow id, false by default
	 * @param includeToolName include tool name in the workflow details if true, true by default
	 * @return all the details information of the queried workflow
	 */
	@GetMapping("/workflows/{workflowId}")
	public WorkflowDetails showWorkflow(
			@PathVariable("workflowId") String workflowId, 
			@RequestParam(required = false) Boolean instance,
			@RequestParam(required = false) Boolean includeToolName,	
			@RequestParam(required = false) Boolean includeInputDetails) {	
		// check permission 
		// Note: Since workflow is not associated with any unit, the AC is checked against any unit
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Workflow, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot view workflow in any unit.");
		}

		try {
			log.info("Retrieving workflow details with ID: " +  workflowId + ", instance: " + instance + ", includeToolName: " + includeToolName);
			return workflowService.showWorkflow(workflowId, instance, includeToolName, includeInputDetails);
		}
		catch (Exception e) {
			throw new GalaxyWorkflowException("Unable to retrieve workflow details with ID " + workflowId + " from Galaxy.", e);
		}
	}
	
}
