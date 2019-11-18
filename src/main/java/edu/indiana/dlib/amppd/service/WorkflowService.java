package edu.indiana.dlib.amppd.service;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

/**
 * Service for workflow related functionalities.
 * @author yingfeng
 *
 */
public interface WorkflowService {

	/**
	 * Return the WorkflowsClient instance.
	 */
	public WorkflowsClient getWorkflowsClient();
	
	/**
	 * Validate the specified workflow and return errors if any.
	 * @param workflowDetails the details of the specified workflow 
	 * @return a list of validation errors for invalid workflow, otherwise empty list 
	 */
	public List<String> validateWorkflow(WorkflowDetails workflowDetails);
	
	/**
	 * Validate the specified workflow and return errors if any.
	 * @param workflowId the ID of the specified workflow 
	 * @return a list of validation errors for invalid workflow, otherwise empty list 
	 */
	public List<String> validateWorkflow(String workflowId);

	/**
	 * Returns true if the specified workflow is valid; false otherwise
	 * @param workflowId the ID of the specified workflow 
	 * @return true if the specified workflow is valid; false otherwise
	 */
	public Boolean isValidWorkflow(String workflowId);

	/**
	 * List workflows currently existing in Galaxy, including/excluding invalid ones based on the specification.
	 * @param excludeInvalid whether to exclude the invalid workflows
	 * @return a list of inquired workflows.
	 */
	public List<Workflow> listWorkflows(Boolean excludeInvalid);
	
	/**
	 * Return the workflow with the given name.
	 * @param workflowName the name of the specified workflow
	 * @return the workflow requested
	 */
	public Workflow getWorkflow(String workflowName);
		
}
