package edu.indiana.dlib.amppd.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class WorkflowResultSearchQuery {
	public WorkflowResultSearchQuery(){
		pageNum = 1;
		resultsPerPage = Integer.MAX_VALUE;
		filterBySubmitters = new String[0];
		filterByWorkflows = new String[0];
		filterByCollections = new String[0];
		filterByItems = new String[0];
		filterByFiles = new String[0];
		filterBySteps = new String[0];
		filterByStatuses = new GalaxyJobState[0];
		filterBySearchTerm = new String[0];
		sortRule = new WorkflowResultSortRule();
		sortRule.setColumnName("id");
		sortRule.setOrderByDescending(false);
		filterByDates = new ArrayList<Date>();
		filterByFinal = false;
	}
	private int pageNum;
	private int resultsPerPage;
	private String[] filterBySubmitters;
	private String[] filterByWorkflows;
	private String[] filterByCollections;
	private String[] filterByItems;
	private String[] filterByFiles;
	private String[] filterBySteps;
	private GalaxyJobState[] filterByStatuses;
	private String[] filterBySearchTerm;	
	private WorkflowResultSortRule sortRule;
	private List <Date> filterByDates;
	private boolean filterByFinal;
}
