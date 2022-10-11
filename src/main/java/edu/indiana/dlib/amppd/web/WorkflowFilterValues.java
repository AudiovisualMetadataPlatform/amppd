package edu.indiana.dlib.amppd.web;

import lombok.Data;

import java.util.List;

@Data
public class WorkflowFilterValues {
    List<String> names;
    List<String> creators;
    List<String> tags;
}
