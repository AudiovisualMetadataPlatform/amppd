package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
	private Long itemId;

	//@NotNull
	@Index
	private Long collectionId;
	
	//@NotNull
	@Index
	@Type(type="text")
	private String primaryfileName;

	//@NotNull
	@Index
	@Type(type="text")
	private String itemName;

	//@NotNull
	@Index
	@Type(type="text")
	private String collectionName;

	//@NotNull
	@Index
	private String externalSource;

	//@NotNull
	@Index
	private String externalId;

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
	
	//@NotNull
	@Index
	private String outputName;	// name of the output
	
	//@NotNull
	@Index
	private String outputType;	// data type file extension of the output

	private String outputPath;	// full absolute path of the output file
	private String outputLink;	// obscure symlink generated for the output file
	
	// we don't use Galaxy downloard URL, so outputUrl can be removed

	private String toolInfo;
	
	//@NotNull
	@Index
	private String submitter;

	//@NotNull
	@Index
	private GalaxyJobState status;

	@Index
	private Boolean relevant;	// indicate if the output dataset is visible thus will display on dashboard by default

	@Index
	private Boolean isFinal;	// indicate if the output isFinal thus will be included in the bag to be exported
		
	//@NotNull
	@Index
	private Date dateCreated;	// job created timestamp from Galaxy job details query

	private Date dateUpdated;	// job updated timestamp from Galaxy job details query
	
	//@NotNull
	@Index
	private Date dateRefreshed;	// timestamp of this record last being refreshed from Galaxy query result

	 @Override
	 public int hashCode() { 
		 return id.intValue();
	 }

	@Override
	public boolean equals(Object result) {
		return result != null && result instanceof WorkflowResult && id.equals(((WorkflowResult)result).getId());
	}

	@Override
	public String toString() {
		String str = "WorkflowResult";
		str += "<id: " + id;
		str += ", primaryfileId: " + primaryfileId;
		str += ", workflowId: " + workflowId;
		str += ", invocationId: " + invocationId;
		str += ", stepId: " + stepId;
		str += ", outputId: " + outputId;
		str += ", historyId: " + historyId;
		str += ", primaryfileName: " + primaryfileName;
		str += ", workflowName: " + workflowName;
		str += ", workflowStep: " + workflowStep;
		str += ", outputName: " + outputName;
		str += ", outputType: " + outputType;
		str += ", submitter: " + submitter;
		str += ", status: " + status;
		str += ", relevant: " + relevant;
		str += ", isFinal: " + isFinal;
		str += ", dateCreated: " + dateCreated;
		str += ", dateRefreshed: " + dateRefreshed;
		str += ">";
		return str;
	}
	
}
