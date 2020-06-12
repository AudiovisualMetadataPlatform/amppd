package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class DashboardSearchQuery {
	private int pageNum;
	private int resultsPerPage;
	private String[] filterBySubmitters;
	private String[] filterBySearchTerm;	
	private DashboardSortRule sortRule;
}
