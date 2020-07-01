package edu.indiana.dlib.amppd.web;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class DashboardFilterValues {
	private List<String> submitters;
	private List<String> workflows;
	private List<String> items;
	private List<String> files;
	private List<String> steps;
	private List<GalaxyJobState> statuses;
	private List<String> searchTerms;
	private List<Date> dateFilter;
}
