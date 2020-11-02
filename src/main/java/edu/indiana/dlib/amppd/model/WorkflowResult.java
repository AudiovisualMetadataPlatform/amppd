package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.jdo.annotations.Index;
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
@Data
@NoArgsConstructor
public class WorkflowResult {
	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;

	//@NotNull
	@Index
	private Long primaryfileId;

	//@NotNull
	@Index
	@Type(type="text")
	private String sourceItem;		// item name	
	
	//@NotNull
	@Index
	@Type(type="text")
	private String sourceFilename;	// primaryfile name

	//@NotNull
	@Index
	private String workflowId;

	//@NotNull
	@Index
	private String invocationId;

	//@NotNull
	@Index
	private String stepId;

	//@NotNull
	@Index(unique="true")		// output dateset ID should be unique
	private String outputId;	// we don't need datasetId as it is the same as outputId

	//@NotNull
	@Index
	private String historyId;
	
	//@NotNull
	@Index
	private String workflowName;

	//@NotNull
	@Index	
	private String workflowStep; // in most cases it's the tool_id of the job in each invocation step
	
	private String toolInfo;
	
	//@NotNull
	@Index
	private String outputFile;	// name of the output
	
	//@NotNull
	@Index
	private String outputType;	// data type file extension of the output

	private String outputPath;	// full absolute path of the output file
	private String outputLink;	// obscure symlink generated for the output file
	
	// we don't use Galaxy downloard URL, so outputUrl can be removed

	//@NotNull
	@Index
	private String submitter;

	//@NotNull
	@Index
	private GalaxyJobState status;

	//@NotNull
	@Index
	private Date dateCreated;	// job created timestamp from Galaxy job details query

	private Date dateUpdated;	// job updated timestamp from Galaxy job details query
	
	//@NotNull
	@Index
	private Date dateRefreshed;	// timestamp of this record last being refreshed from Galaxy query result

	@Index
	private Boolean isFinal;	// indicate if the output isFinal thus will be included in the bag to be exported
	
}
