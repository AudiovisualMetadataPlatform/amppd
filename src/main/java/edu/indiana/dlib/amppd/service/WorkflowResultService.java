package edu.indiana.dlib.amppd.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

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
	 * Refreshes WorkflowResults table iteratively by retrieving and processing workflow invocations per primaryfile.
	 * Use this method instead of refreshWorkflowResultsLumpsum if request to Galaxy tends to timeout due to large amount of records.
	 * @return the list of WorkflowResults refreshed
	 */
	public List<WorkflowResult> refreshWorkflowResultsIterative();

	/**
	 * Refreshes WorkflowResults table by retrieving and processing all workflow invocations at once.
	 * Use this method only if invocations in Galaxy are within a limited volume that can be retrieved in a lump sum manner.
	 * @return the list of WorkflowResults refreshed
	 */
	public List<WorkflowResult> refreshWorkflowResultsLumpsum();

	/**
	 * Refreshes incomplete workflow results status values.
	 */
	public List<WorkflowResult> refreshIncompleteWorkflowResults();
		
	/**
	 * Fix workflow results with obsolete output types with correct data types and update the corresponding datasets in Galaxy.
	 * @return the list of WorkflowResults updated
	 */
	public Set<WorkflowResult> fixWorkflowResultsOutputType();
	
	/**
	 * This method is deprecated, please use setWorkflowResultsRelevant instead.
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
	public Set<WorkflowResult> setWorkflowResultsRelevant(List<Map<String, String>> workflowStepOutputs, Boolean relevant);
	
	/**
	 * Sets the specified WorkflowResult according to the specified final status.
	 * @param WorkflowResultId id of the specified WorkflowResult
	 * @param isFinal the specified final status
	 * @return the WorkflowResult updated
	 */
	public WorkflowResult setWorkflowResultFinal(Long workflowResultId, Boolean isFinal);

	/**
	 * Set and export workflow result csv file as part of reponse
	 * @param response HttpServletResponse
	 * @param query WorkflowResultSearchQuery
	 * @return the list of WorkflowResults updated
	 */
	public List<WorkflowResult> exportWorkflowResults(HttpServletResponse response, WorkflowResultSearchQuery query);
	
}
