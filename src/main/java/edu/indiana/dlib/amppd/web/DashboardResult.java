package edu.indiana.dlib.amppd.web;

import java.util.Date;
import lombok.Data;

@Data
public class DashboardResult {
	private Date date;
	private String submitter;
	private String workflowName;
	private String sourceItem;
	private String sourceFilename;
	private String workflowStep;
	private String outputFile;
	private GalaxyJobState status;
}
