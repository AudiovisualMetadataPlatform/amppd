package edu.indiana.dlib.amppd.web;

import lombok.Data;

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
    }

    private int pageNum;
    private int resultsPerPage;
    private WorkflowResultSortRule sortRule;

    private Long[] filterByCategories;
    private Long[] filterByMstTools;
}


