package edu.indiana.dlib.amppd.web;

import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
public class DashboardSearchQuery {
	private int pageNum;
	private int resultsPerPage;
	private String[] filterBySubmitters;
	private String[] filterBySearchTerm;	
	private DashboardSortRule sortRule;
	private List <Date> filterByDates;
}
