package edu.indiana.dlib.amppd.web;

import java.util.List;

import edu.indiana.dlib.amppd.model.WorkflowResult;
import lombok.Data;

@Data
public class WorkflowResultResponse {
	private List<WorkflowResult> rows;
	private int totalResults;
	private WorkflowResultFilterValues filters;
}
