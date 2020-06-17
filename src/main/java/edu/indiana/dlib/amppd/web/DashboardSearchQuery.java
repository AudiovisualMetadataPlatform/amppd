package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class DashboardSearchQuery {
	private int pageNum;
	private int resultsPerPage;
	private String[] filterBySubmitters;
	private String[] filterByWorkflows;
	private String[] filterByItems;
	private String[] filterByFiles;
	private String[] filterBySteps;
	private String[] filterByStatuses;
	private String[] filterBySearchTerm;	
	private DashboardSortRule sortRule;
}
