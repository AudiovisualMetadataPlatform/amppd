package edu.indiana.dlib.amppd.web;

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import lombok.Data;

import java.util.List;

@Data
public class WorkflowResponse {
    private List<Workflow> rows;
    private int totalResults;
    private WorkflowFilterValues filters;
}