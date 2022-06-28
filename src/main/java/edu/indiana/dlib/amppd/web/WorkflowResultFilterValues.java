package edu.indiana.dlib.amppd.web;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class WorkflowResultFilterValues {
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
	private List<GalaxyJobState> statuses;
	private List<String> searchTerms;
}
