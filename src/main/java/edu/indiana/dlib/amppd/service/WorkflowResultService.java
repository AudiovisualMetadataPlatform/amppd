package edu.indiana.dlib.amppd.service;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;

import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;

public interface WorkflowResultService {
	/***
	 * Get a list of all results to display in the workflow results dashboard and deliverables UI.
	 * @return
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
	 * Sets the specified WorkflowResult according to the specified final status
	 * @param WorkflowResultId id of the specified WorkflowResult
	 * @param isFinal the specified final status
	 * @return
	 */
	public boolean setResultIsFinal(long workflowResultId, boolean isFinal);
	
	/**
	 * Refreshes incomplete workflow results status values
	 */
	public void refreshIncompleteResults();
	
	/**
	 * Hide all irrelevant workflow results by setting its corresponding output dataset in Galaxy to invisible,
	 * and remove the row from the WorkflowResult table.
	 */
	public void hideIrrelevantWorkflowResults();
	
}
