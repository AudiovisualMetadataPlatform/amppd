package edu.indiana.dlib.amppd.web;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class MgmEvaluationTestFilters {
    private List<Date> dateFilter;
    private List<String> submitters;
    private List<WorkflowResultFilterUnit> units;
    private List<WorkflowResultFilterCollection> collections;
    private List<WorkflowResultFilterItem> items;
    private List<WorkflowResultFilterFile> files;
    private List<String> externalIds;
    private List<String> workflows;
    private List<String> steps;
    private List<String> outputs;
    private List<String> types;
    private List<GalaxyJobState> statuses;
    private List<String> searchTerms;
    private List<Date> testDateFilter;
}
