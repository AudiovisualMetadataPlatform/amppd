package edu.indiana.dlib.amppd.service;

import java.util.List;

import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

import edu.indiana.dlib.amppd.model.DashboardResult;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.web.DashboardResponse;
import edu.indiana.dlib.amppd.web.DashboardSearchQuery;

public interface DashboardService {
	/***
	 * Get a list of all results to display in the workflow dashboard
	 * @return
	 */
	public DashboardResponse getDashboardResults(DashboardSearchQuery query);
	
	/***
	 * Get all final results for the specified primaryfile.
	 * @param primaryfileId ID of the specified primaryfile
	 * @return the list of final results for the specified primaryfile
	 */
	public List<DashboardResult> getFinalDashboardResults(Long primaryfileId);
	
	/**
	 * Adds initial results for the given invocation upon submitting the given primaryfile to the give workflow.
	 * @param invocation the given invocation
	 * @param workflow the give workflow
	 * @param primaryfile the given primaryfile
	 * @return the list of DashboardResults added
	 */
	public List<DashboardResult> addDashboardResults(Invocation invocation, Workflow workflow, Primaryfile primaryfile);	
	
	/**
	 * Refreshes DashboardResults table iteratively by retrieving and processing workflow invocations per primaryfile.
	 * Use this method instead of refreshDashboardResultsLumpsum if request to Galaxy tends to timeout due to large amount of records.
	 * @return the list of DashboardResults refreshed
	 */
	public List<DashboardResult> refreshDashboardResultsIterative();

	/**
	 * Refreshes DashboardResults table by retrieving and processing all workflow invocations at once.
	 * Use this method only if invocations in Galaxy are within a limited volume that can be retrieved in a lump sum manner.
	 * @return the list of DashboardResults refreshed
	 */
	public List<DashboardResult> refreshDashboardResultsLumpsum();

	/**
	 * Sets the specified dashboardResult according to the specified final status
	 * @param dashboardResultId id of the specified dashboardResult
	 * @param isFinal the specified final status
	 * @return
	 */
	public boolean setResultIsFinal(long dashboardResultId, boolean isFinal);
}
