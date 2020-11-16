package edu.indiana.dlib.amppd.web;

import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import lombok.Data;

@Data
public class WorkflowOutputResult {
	private WorkflowOutputs result;
	private boolean success;
	private String collectionLabel;
	private String itemLabel;
	private String fileLabel;
	private String fileName;
	private String error;
	private long primaryfileId;
	//collection label, item label, file label, and file name
}
