package edu.indiana.dlib.amppd.web;

import lombok.Data;

import java.util.List;

@Data
public class MgmEvaluationTestResponse {
    private List<MgmEvaluationTestResult> rows;
    private int totalResults;
    private WorkflowResultFilterValues filters;
}
