package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import lombok.Getter;
import lombok.extern.java.Log;

/**
 * Implementation of WorkflowService.
 * @author yingfeng
 *
 */
@Service
@Log
public class WorkflowServiceImpl implements WorkflowService {
	
	@Autowired
	private GalaxyApiService galaxyApiService;
	
	@Getter
	private WorkflowsClient workflowsClient;
		
	/**
	 * Initialize the WorkflowServiceImpl bean.
	 */
	@PostConstruct
	public void init() {
		workflowsClient = galaxyApiService.getGalaxyInstance().getWorkflowsClient();
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.isValidWorkflow(WorkflowDetails)
	 */	
	@Override	
	public List<String> validateWorkflow(WorkflowDetails workflowDetails) {
		List<String> errors = new ArrayList<String>();

		if (workflowDetails == null) {
			errors.add("The workflow is null.");
		}
		else if (workflowDetails.getInputs() == null) {
			errors.add("The workflow has null inputs.");
		}
		else if (workflowDetails.getInputs().keySet().size() == 0) {
			errors.add("The workflow has no inputs.");
		}
		// for AMPPD we require that a workflow only takes one primaryfile as input
		else if (workflowDetails.getInputs().keySet().size() > 1) {
			errors.add("The workflow has more than one inputs.");
		}
		
		// At this point, we only check workflow input; in the future we might add more validation rules.
		return errors;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.validateWorkflow(String)
	 */	
	@Override	
	public  List<String> validateWorkflow(String workflowId) {
		try {
			return validateWorkflow(workflowsClient.showWorkflow(workflowId));
		}
		catch (Exception e) {
			throw new GalaxyWorkflowException("Exception when retrieving details for workflow " + workflowId);
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.isValidWorkflow(String)
	 */	
	@Override		
	public Boolean isValidWorkflow(String workflowId) {		
		return validateWorkflow(workflowId).size() == 0;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.listWorkflows(Boolean)
	 */	
	@Override	
	public List<Workflow> listWorkflows(Boolean excludeInvalid) {
		List<Workflow> workflows = null;
		
		try {
			workflows = workflowsClient.getWorkflows();
		}
		catch (Exception e) {
			String msg = "Unable to retrieve workflows from Galaxy.";
			log.severe(msg);
			throw new GalaxyWorkflowException(msg, e);
		}
		
		if (!excludeInvalid) {
			log.info("Found total of " + workflows.size() + " workflows currently existing in Galaxy: " + workflows);
			return workflows;			
		}
		
		List<Workflow> validWorkflows = new ArrayList<Workflow>();
		for (Workflow workflow : workflows) {
			if (isValidWorkflow(workflow.getId())) {
				validWorkflows.add(workflow);
			}
		}

		log.info("Found " + validWorkflows.size() + " valid workflows among a total of" + workflows.size() + " workflows in Galaxy:" + validWorkflows);
		return validWorkflows;			
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.getWorkflow(String)
	 */	
	@Override
	public Workflow getWorkflow(String workflowName) {
		for (Workflow workflow : workflowsClient.getWorkflows()) {
			if (workflow.getName().equalsIgnoreCase(workflowName)) {
				return workflow;			
			}
		}
		return null;
	}
	
}
