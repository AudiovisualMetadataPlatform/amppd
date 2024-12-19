package edu.indiana.dlib.amppd.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
		
		String published = showPublished == null ? "null" : showPublished.toString();
		String hidden = showHidden == null ? "null" : showHidden.toString();
		String deleted = showDeleted == null ? "null" : showDeleted.toString();
		log.info("Listing workflows in Galaxy, showPublished = " + published + ", showHidden = " + hidden + ", showDeleted = " + deleted);

		try {
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
			@PathVariable String workflowId, 
			@RequestParam(required = false) Boolean instance,
			@RequestParam(required = false) Boolean includeToolName,	
			@RequestParam(required = false) Boolean includeInputDetails) {	
		// check permission 
		// Note: Since workflow is not associated with any unit, the AC is checked against any unit
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Workflow, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot view workflow in any unit.");
		}

		log.info("Retrieving workflow details with ID: " +  workflowId + ", instance: " + instance + ", includeToolName: " + includeToolName);
		try {
			return workflowService.showWorkflow(workflowId, instance, includeToolName, includeInputDetails);
		}
		catch (Exception e) {
			throw new GalaxyWorkflowException("Unable to retrieve workflow details with ID " + workflowId + " from Galaxy.", e);
		}
	}
	
	/**
	 * Update the given workflow with the given publish/unpublish and/or activate/deactivate flags.
	 * @param workflowId ID of the given workflow
	 * @param publish true to publish the workflow; false to unpublish the it; null for no change
	 * @param activate true to activate the workflow; false to deactivate the it; null for no change
	 * @return the updated workflow	 
	 */
	@PatchMapping("/workflows/{workflowId}")
	public WorkflowDetails updateWorkflow(
			@PathVariable String workflowId, 
			@RequestParam(required = false) Boolean publish,
			@RequestParam(required = false) Boolean activate) {
		// check permission: Note: 
		// Since workflow is not associated with any unit, the AC is checked against any unit;
		// Also, Restrict-Workflow refers to updating metadata including published/active(hidden) fields on workflow.
		boolean can = permissionService.hasPermission(ActionType.Restrict, TargetType.Workflow, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update metadata of workflow in any unit.");
		}
		
		try {
			log.info("Updating metadata of workflow with ID: " +  workflowId + ", publish: " + publish + ", activate: " + activate);
			return workflowService.updateWorkflow(workflowId, publish, activate);
		}
		catch (GalaxyWorkflowException e) {
			// in ccase of GalaxyWorkflowException, inform client about the cause and response with 409 Conflict error code
			String reason = "Workflow " + workflowId + " can't be deactivated as it is involved in some on-going invocations.";
			throw new ResponseStatusException(HttpStatus.CONFLICT, reason, e);
		}
		catch (Exception e) {
			throw new GalaxyWorkflowException("Unable to update workflow with ID " + workflowId + " in Galaxy.", e);
		}
	}
		
}
