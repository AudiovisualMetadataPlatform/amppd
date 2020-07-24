package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.metamodel.StaticMetamodel;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import edu.indiana.dlib.amppd.web.GalaxyJobState;
import lombok.Data;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Data
public class DashboardResult {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	private Date date;
	private String submitter;
	private String workflowName;
	private String sourceItem;
	private Long primaryfileId;
	private String sourceFilename;
	private String workflowStep;
	private String outputFile;
	private String outputType;	// type of output file, indicated by its file extension 
	private String outputPath;	// full absolute path of the output file
	private String outputLink;	// obscure symlink generated for the output file
	private String outputUrl;	// full download URL in Galaxy
	private GalaxyJobState status;
	private String workflowId;
	private String historyId;
	private String outputId;	// we don't need datasetId as it is the same as outputId
	private String invocationId;
	private Date updateDate;
	private String stepId;
}
