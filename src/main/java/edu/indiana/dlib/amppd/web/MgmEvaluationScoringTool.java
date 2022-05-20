package edu.indiana.dlib.amppd.web;

import lombok.Data;

@Data
public class MgmEvaluationScoringTool {

	String name;
	String description;
	String version;
	String upgradeDate;
	String workflowResultDataType;
	String groundTruthFormat;
	String parameters;
	String scriptPath;
	String sectionId;
	String mgmToolId;
}
