package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.web.DashboardResponse;
import edu.indiana.dlib.amppd.web.DashboardResult;
import edu.indiana.dlib.amppd.web.DashboardSearchQuery;

public interface DashboardService {
	/***
	 * Get a list of all results to display in the workflow dashboard
	 * @return
	 */
	DashboardResponse getDashboardResults(DashboardSearchQuery query);
	/**
	 * Adds initial record to the dashboard results in database
	 * @param workflowId
	 * @param workflowName
	 * @param primaryfileId
	 * @param historyId
	 */
	void addDashboardResult(String workflowId, String workflowName, long primaryfileId, String historyId);
	/**
	 * Refreshes the database.  Should be used for initial population of database only. 
	 * @return
	 */
	List<DashboardResult> refreshAllDashboardResults();
}
