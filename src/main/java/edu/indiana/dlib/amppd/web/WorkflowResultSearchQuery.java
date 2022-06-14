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
		filterByUnits = new Long[0];
		filterByCollections = new Long[0];
		filterByItems = new Long[0];
		filterByFiles = new Long[0];
		filterByExternalIds = new String[0];
		filterByWorkflows = new String[0];
		filterBySteps = new String[0];
		filterByOutputs = new String[0];
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
	private Long[] filterByUnits;
	private Long[] filterByCollections;
	private Long[] filterByItems;
	private Long[] filterByFiles;
	private String[] filterByExternalIds;
	private String[] filterByWorkflows;
	private String[] filterBySteps;
	private String[] filterByOutputs;
	private GalaxyJobState[] filterByStatuses;
	private String[] filterBySearchTerms;	
	private boolean filterByRelevant;
	private boolean filterByFinal;
	private WorkflowResultSortRule sortRule;
	
	/**
	 * Get the filter value for the specified WorkflowResult field of String type.
	 * @param field the specified WorkflowResult field
	 * @return the filter value for the field
	 */
	public String[] getFilterBy(String field) {
		switch (field) {
		case "submitter":
			return filterBySubmitters;
		case "externalId":
			return filterByExternalIds;
		case "workflowName":
			return filterByWorkflows;
		case "workflowStep":
			return filterBySteps;
		case "outputName":
			return filterByOutputs;
		default:
			return null;
		}
	}
	
	/**
	 * Set the specified filter values for the specified WorkflowResult field of String type.
	 * @param field the specified WorkflowResult field
	 * @param values the filter values to set
	 */
	public void setFilterBy(String field, String[] values) {
		switch (field) {
		case "submitter":
			setFilterBySubmitters(values);
		case "externalId":
			setFilterByExternalIds(values);
		case "workflowName":
			setFilterByWorkflows(values);
		case "workflowStep":
			setFilterBySteps(values);
		case "outputName":
			setFilterByOutputs(values);
		}
	}
	
}
