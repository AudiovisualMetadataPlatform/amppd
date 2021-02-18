package edu.indiana.dlib.amppd.web;

import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import lombok.Data;

@Data
public class WorkflowOutputResult {
	private Boolean success;
	private String error;
	private Long primaryfileId;
	private String collectionName;
	private String itemName;
	private String primaryfileName;
	private WorkflowOutputs outputs;
}
