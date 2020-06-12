package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class DashboardSortRule {
	String columnName;
	boolean orderByDescending;
}
