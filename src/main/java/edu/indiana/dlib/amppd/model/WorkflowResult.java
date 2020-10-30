package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import edu.indiana.dlib.amppd.web.GalaxyJobState;
import lombok.Data;
import lombok.NoArgsConstructor;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "workflow_result")
@Data
@NoArgsConstructor
public class WorkflowResult {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	private Long primaryfileId;
	@Type(type="text")
	private String sourceItem;		// item name	
	@Type(type="text")
	private String sourceFilename;	// primaryfile name

	private String workflowId;
	private String invocationId;
	private String stepId;
	private String outputId;	// we don't need datasetId as it is the same as outputId
	private String historyId;
	
	private String workflowName;
	private String workflowStep; // in most cases it's the tool_id of the job in each invocation step
	private String toolInfo;
	
	private String outputFile;	// name of the output
	private String outputType;	// data type file extension of the output
	private String outputPath;	// full absolute path of the output file
	private String outputLink;	// obscure symlink generated for the output file
	
	// we don't use Galaxy downloard URL, so outputUrl can be removed

	private String submitter;
	private GalaxyJobState status;
	private Date dateCreated;	// job created timestamp from Galaxy job details query
	private Date dateUpdated;	// job updated timestamp from Galaxy job details query
	
	private Date dateRefreshed;	// timestamp of this record last being refreshed from Galaxy query result
	private Boolean isFinal;	// indicate if the output isFinal thus will be included in the bag to be exported
}
