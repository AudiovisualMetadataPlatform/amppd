package edu.indiana.dlib.amppd.repository;

import edu.indiana.dlib.amppd.web.DashboardResponse;
import edu.indiana.dlib.amppd.web.DashboardSearchQuery;

public interface DashboardRepositoryCustom {
	public DashboardResponse searchResults(DashboardSearchQuery searchQuery);
}