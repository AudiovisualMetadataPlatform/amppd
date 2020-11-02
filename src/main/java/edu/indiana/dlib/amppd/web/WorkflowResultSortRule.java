package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class WorkflowResultSortRule {
	String columnName;
	boolean orderByDescending;
}
