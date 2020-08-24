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

	private Long primaryfileId;
	private String sourceItem;
	private String sourceFilename;

	private String workflowId;
	private String invocationId;
	private String stepId;
	private String outputId;	// we don't need datasetId as it is the same as outputId
	private String historyId;
	
	private String workflowName;
	private String workflowStep;
	private String toolVersion;
	
	private String outputFile;
	private String outputType;
	private String outputPath;	// full absolute path of the output file
	private String outputLink;	// obscure symlink generated for the output file
	private String outputUrl;	// full download URL in Galaxy

	private String submitter;
	private GalaxyJobState status;
	private Date date;
	private Date updateDate;
	private Boolean isFinal;	// indicate if the output isFinal thus will be included in the bag to be exported
}
