package edu.indiana.dlib.amppd.web;

import java.util.List;

import edu.indiana.dlib.amppd.model.DashboardResult;
import lombok.Data;

@Data
public class DashboardResponse {
	private List<DashboardResult> rows;
	private int totalResults;
	private DashboardFilterValues filters;
}
