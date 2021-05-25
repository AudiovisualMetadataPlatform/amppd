package edu.indiana.dlib.amppd.service;

import java.util.List;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

/**
 * Service for workflow related functionalities.
 * @author yingfeng
 *
 */
public interface WorkflowService {

	/**
	 * List workflows satisfying the given criteria.
	 * @param showUnpublished: if true, include also unpublished (in addition to published) workflows; otherwise include only published workflows
	 * @param showHidden: if true, include only hidden workflows; otherwise include only unhidden workflows
	 * @param showDeleted: if true, include only deleted workflows; otherwise include only undeleted workflows
	 * @return workflows satisfying the given criteria
	 */
	public List<Workflow> getWorkflows(Boolean showPublished, Boolean showHidden, Boolean showDeleted);
	
	/**
	 * Get the WorkflowsClient instance.
	 */
	public WorkflowsClient getWorkflowsClient();
	
	/**
	 * Get the workflow with the specified name.
	 * @param workflowName the name of the specified workflow
	 * @return the workflow requested
	 */
	public Workflow getWorkflow(String workflowName);
	
	/**
	 * Get the name of the specified workflow and store it in a local cache: 
	 * first, search in the local cache;
	 * if not found. query Galaxy;
	 * if still not found, use the ID as the name.
	 * @param workflowId the ID of the specified workflow
	 * @return the workflow name
	 */
	public String getWorkflowName(String workflowId);
	
	/**
	 * Clear up the workflow names cache to its initial state. 
	 */
	public void clearWorkflowNamesCache();
	
	/**
	 * Returns the size of the workflow names cache. 
	 */
	public Integer workflowNamesCacheSize();
	
}
