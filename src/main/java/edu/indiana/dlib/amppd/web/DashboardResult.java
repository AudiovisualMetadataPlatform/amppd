package edu.indiana.dlib.amppd.web;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Data
public class DashboardResult {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private Date date;
	private String submitter;
	private String workflowName;
	private String sourceItem;
	private String sourceFilename;
	private String workflowStep;
	private String outputFile;
	private GalaxyJobState status;
	private String workflowId;
	private String historyId;
	private String outputId;
	private String invocationId;
	private Date updateDate;
	private String datasetId;
	private String stepId;
}
