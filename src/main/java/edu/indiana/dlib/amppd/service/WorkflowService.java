package edu.indiana.dlib.amppd.service;

import java.util.Date;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

import edu.indiana.dlib.amppd.web.WorkflowResponse;

/**
 * Service for workflow related functionalities.
 * @author yingfeng
 *
 */
public interface WorkflowService {

	/**
	 * Get the WorkflowsClient instance.
	 */
	public WorkflowsClient getWorkflowsClient();
	
	/**
	 * Returns true if the given workflow has the given tag; false otherwise.
	 * @param workflow the given workflow
	 * @param tag the given tag
	 * @return true if the workflow contains the tag; false otherwise.
	 */
	public Boolean hasWorkflowTag(Workflow workflow, String tag);

	/**
	 * Returns true if the given workflow is published, i.e. its published field is true or it has the published tag; false otherwise.
	 * @param workflow the given workflow
	 * @return true if the workflow is published; false otherwise.
	 */
	public Boolean isWorkflowPublished(Workflow workflow);

	/**
	 * List workflows according to the given criteria.
	 * @param showPublished: if None/True/False, include both, only published, or only unpublished workflows
	 * @param showHidden: if true, include only hidden workflows; otherwise include only unhidden workflows
	 * @param showDeleted: if true, include only deleted workflows; otherwise include only undeleted workflows
	 * @return workflows satisfying the given criteria
	 */
	public WorkflowResponse listWorkflows(Boolean showPublished, Boolean showHidden, Boolean showDeleted, String[] tag, String[] name, String[] annotations, String[] creators, Date[] dateRange);
	
	/**
	 * Show details of a workflow based on information retrieved from Galaxy.
	 * Note: Set instance to true if the workflow ID is returned from invocation listing, in which case it's likely not a StoredWorkflow ID.
	 * @param workflowId ID of the queried workflow
	 * @param instance true if fetch by Workflow ID instead of StoredWorkflow id; false by default
	 * @param includeToolName include tool name in the workflow details if true; true by default
	 * @param includeInputDetails include details about input such as data type and data source if true; true by default
	 * @return all the details information of the queried workflow
	 */
	public WorkflowDetails showWorkflow(String workflowName, Boolean instance, Boolean includeToolName, Boolean includeInputDetails);
	
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

	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.clearWorkflowsCache()
	 */
	public void clearWorkflowsCache();

}
