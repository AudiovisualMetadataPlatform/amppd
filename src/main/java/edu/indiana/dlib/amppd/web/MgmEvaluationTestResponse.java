package edu.indiana.dlib.amppd.web;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class MgmEvaluationTestResponse {
    private List<Map> rows;
    private int totalResults;
    private WorkflowResultFilterValues filters;
}
