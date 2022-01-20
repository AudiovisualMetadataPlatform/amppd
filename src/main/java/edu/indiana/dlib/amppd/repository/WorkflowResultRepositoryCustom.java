package edu.indiana.dlib.amppd.repository;

import edu.indiana.dlib.amppd.web.WorkflowResultFilterValues;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;

public interface WorkflowResultRepositoryCustom {
	public WorkflowResultResponse findByQuery(WorkflowResultSearchQuery searchQuery);
}