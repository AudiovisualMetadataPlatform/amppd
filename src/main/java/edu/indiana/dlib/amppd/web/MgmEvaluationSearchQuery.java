package edu.indiana.dlib.amppd.web;

import lombok.Data;

import java.util.Date;

@Data
public class MgmEvaluationSearchQuery {
    public MgmEvaluationSearchQuery(){
        pageNum = 1;
        sortRule = new WorkflowResultSortRule();
        sortRule.setColumnName("id");
        sortRule.setOrderByDescending(false);

        resultsPerPage = Integer.MAX_VALUE;
        filterByCategories = new Long[0];
        filterByMstTools = new Long[0];
        filterByDates = new Date[0];
        filterBySubmitters = new String[0];
        filterByUnits = new Long[0];
        filterByCollections = new Long[0];
        filterByItems = new Long[0];
        filterByFiles = new Long[0];
        filterByExternalIds = new String[0];
        filterByWorkflows = new String[0];
        filterBySteps = new String[0];
        filterByOutputs = new String[0];
        filterByTypes = new String[0];
        filterByStatuses = new GalaxyJobState[0];
        filterBySearchTerms = new String[0];
        filterByRelevant = false;
        filterByFinal = false;
        filterByTestDates = new Date[0];
    }

    private int pageNum;
    private int resultsPerPage;
    private WorkflowResultSortRule sortRule;

    private Long[] filterByCategories;
    private Long[] filterByMstTools;
    private Date[] filterByDates;
    private String[] filterBySubmitters;
    private Long[] filterByUnits;
    private Long[] filterByCollections;
    private Long[] filterByItems;
    private Long[] filterByFiles;
    private String[] filterByExternalIds;
    private String[] filterByWorkflows;
    private String[] filterBySteps;
    private String[] filterByOutputs;
    private String[] filterByTypes;
    private GalaxyJobState[] filterByStatuses;
    private String[] filterBySearchTerms;
    private boolean filterByRelevant;
    private boolean filterByFinal;

    private Date[] filterByTestDates;
    /**
     * Get the filter value for the specified WorkflowResult field of String type.
     * @param field the specified WorkflowResult field
     * @return the filter value for the field
     */
    public String[] getFilterByField(String field) {
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
            case "outputType":
                return filterByTypes;
            default:
                // return null for other fields
                return null;
        }
    }

    /**
     * Set the specified filter values for the specified WorkflowResult field of String type.
     * @param field the specified WorkflowResult field
     * @param values the filter values to set
     */
    public void setFilterByField(String field, String[] values) {
        switch (field) {
            case "submitter":
                setFilterBySubmitters(values);
                break;
            case "externalId":
                setFilterByExternalIds(values);
                break;
            case "workflowName":
                setFilterByWorkflows(values);
                break;
            case "workflowStep":
                setFilterBySteps(values);
                break;
            case "outputName":
                setFilterByOutputs(values);
                break;
            case "outputType":
                setFilterByTypes(values);
                break;
            default:
                // do nothing for other fields
                break;
        }
    }
}


