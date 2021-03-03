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
		filterByDates = new ArrayList<Date>();
		filterBySubmitters = new String[0];
		filterByCollections = new String[0];
		filterByItems = new String[0];
		filterByFiles = new String[0];
		filterByWorkflows = new String[0];
		filterBySteps = new String[0];
		filterByStatuses = new GalaxyJobState[0];
		filterBySearchTerms = new String[0];
		filterByRelevant = false;
		filterByFinal = false;
		sortRule = new WorkflowResultSortRule();
		sortRule.setColumnName("id");
		sortRule.setOrderByDescending(false);
	}
	private int pageNum;
	private int resultsPerPage;
	private List <Date> filterByDates;
	private String[] filterBySubmitters;
	private String[] filterByCollections;
	private String[] filterByItems;
	private String[] filterByFiles;
	private String[] filterByWorkflows;
	private String[] filterBySteps;
	private GalaxyJobState[] filterByStatuses;
	private String[] filterBySearchTerms;	
	private boolean filterByRelevant;
	private boolean filterByFinal;
	private WorkflowResultSortRule sortRule;
}
