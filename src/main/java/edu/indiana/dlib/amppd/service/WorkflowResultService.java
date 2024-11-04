package edu.indiana.dlib.amppd.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;

public interface WorkflowResultService {
	/**
	 * Get a list of all workflow results satisfying the given query.
	 * @param query the search query for workflow results
	 * @return the WorkflowResultResponse containing the list of queried workflow results
	 */
	public WorkflowResultResponse getWorkflowResults(WorkflowResultSearchQuery query);
	
	/***
	 * Get all final results for the specified primaryfile.
	 * @param primaryfileId ID of the specified primaryfile
	 * @return the list of final results for the specified primaryfile
	 */
	public List<WorkflowResult> getFinalWorkflowResults(Long primaryfileId);
	
	/**
	 * Adds initial results for the given invocation upon submitting the given primaryfile to the give workflow.
	 * @param invocation the given invocation
	 * @param workflow the give workflow
	 * @param primaryfile the given primaryfile
	 * @return the list of WorkflowResults added
	 */
	public List<WorkflowResult> addWorkflowResults(Invocation invocation, Workflow workflow, Primaryfile primaryfile);	
	
	/**
	 * Delete workflow results associated with the specified collection if inactive; otherwise no action.
	 * @collection the specified collection
	 * @return the list of deleted workflow results
	 */
	public List<WorkflowResult> deleteInactiveWorkflowResults(Collection collection);
	
	/**
	 * Refresh status for all WorkflowResults whose output status might still change by job runners in Galaxy.
	 */
	public List<WorkflowResult> refreshIncompleteWorkflowResults();

	/**
	 * Refreshes WorkflowResults table iteratively by retrieving and processing workflow invocations per primaryfile.
	 * Use this method instead of refreshWorkflowResultsLumpsum if request to Galaxy tends to timeout due to large amount of records.
	 * @return the list of WorkflowResults refreshed
	 */
	public List<WorkflowResult> refreshWorkflowResultsIterative();

	/**
	 * Refreshes WorkflowResults table by retrieving and processing all workflow invocations at once.
	 * Use this method only if invocations in Galaxy are within a limited volume that can be retrieved in a lump sum manner.
	 * @return the list of WorkflowResults refreshed
	 * @deprecated This method does not exclude workflow results from inactive primaryfiles. 
	 * Use refreshWorkflowResultsIterative instead for better consistency and performance.
	 */
	@Deprecated
	public List<WorkflowResult> refreshWorkflowResultsLumpsum();
		
	/**
	 * Fix workflow results with obsolete output types with correct data types and update the corresponding datasets in Galaxy.
	 * @return the list of WorkflowResults updated
	 */
	public Set<WorkflowResult> fixWorkflowResultsOutputType();
	
	/**
	 * This method is deprecated, please use setRelevantWorkflowResults instead.
	 * Hide all irrelevant workflow results by setting their corresponding output datasets in Galaxy to invisible,
	 * and remove the row from the WorkflowResult table.
	 * @return the list of WorkflowResults updated
	 */
	@Deprecated
	public Set<WorkflowResult> hideIrrelevantWorkflowResults();

	/**
	 * Set the WorkflowResults matching the given list of workflow-step-output maps as relevant/irrelevant, 
	 * and update their corresponding output datasets in Galaxy as visible/invisible accordingly.
	 * Note that if a wild card is used in a field of a search criteria map, then that criteria matches all values of that field.
	 * @param workflowStepOutputs the list of workflowId-workflowStep-outputName maps identifying the results to be set
	 * @param relevant indicator on whether or not to set WorkflowResults as relevant
	 * @return the list of WorkflowResults updated
	 */
	public Set<WorkflowResult> setRelevantWorkflowResults(List<Map<String, String>> workflowStepOutputs, Boolean relevant);
	
	/**
	 * Update the specified WorkflowResult according to the specified output label and final status;
	 * if outputLabel or isFinal is not provided, then no update on the corresponding field. 
	 * @param workflowResultId id of the specified WorkflowResult
	 * @param outputLabel the specified output label
	 * @param isFinal the specified final status
	 * @return WorkflowResult updated
	 */
	public WorkflowResult updateWorkflowResult(Long workflowResultId, String outputLabel, Boolean isFinal);

	/**
	 * Update the specified WorkflowResult according to the specified output label and final status;
	 * if outputLabel or isFinal is not provided, then no update on the corresponding field. 
	 * @param workflowResult the specified WorkflowResult
	 * @param outputLabel the specified output label
	 * @param isFinal the specified final status
	 * @return WorkflowResult updated
	 */
	public WorkflowResult updateWorkflowResult(WorkflowResult workflowResult, String outputLabel, Boolean isFinal);

	/**
	 * Delete the specified WorkflowResult from AMP table and Galaxy history.
	 * @param workflowResultId id of the specified WorkflowResult
	 * @return WorkflowResult deleted
	 */
	public WorkflowResult deleteWorkflowResult(Long workflowResultId);

    /**
	 * Delete the specified WorkflowResult from AMP table and Galaxy history.
	 * @param workflowResult the specified WorkflowResult
	 * @return WorkflowResult deleted
	 */
	public WorkflowResult deleteWorkflowResult(WorkflowResult workflowResult);

    /**
	 * Delete the WorkflowResults associated with the specified Dataentity from AMP table and Galaxy.
     * @param entity the specified Dataentity
     * @return the list of WorkflowResults deleted
     */
    public List<WorkflowResult> deleteWorkflowResults(Dataentity dataentity);
    
	/**
	 * Set and export workflow result csv file as part of reponse
	 * @param response HttpServletResponse
	 * @param query WorkflowResultSearchQuery
	 * @return the list of WorkflowResults updated
	 */
	public List<WorkflowResult> exportWorkflowResults(HttpServletResponse response, WorkflowResultSearchQuery query);
	
}
