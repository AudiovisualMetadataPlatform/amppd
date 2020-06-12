package edu.indiana.dlib.amppd.web;

import java.util.List;

import lombok.Data;

@Data
public class DashboardFilterValues {
	private List<String> submitters;
	private List<String> searchTerms;
}
