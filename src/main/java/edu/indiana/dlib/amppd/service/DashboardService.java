package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.web.DashboardResult;

public interface DashboardService {
	/***
	 * Get a list of all results to display in the workflow dashboard
	 * @return
	 */
	List<DashboardResult> getDashboardResults();
	List<DashboardResult> getAllDashboardResults();
	void addDashboardResult(String workflowId, String workflowName, long primaryfileId, String historyId);
	List<DashboardResult> refreshAllDashboardResults();
}
