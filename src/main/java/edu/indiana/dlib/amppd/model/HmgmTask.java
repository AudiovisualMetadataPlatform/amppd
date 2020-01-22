package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Class representing a Human MGM task, which is performed by an HMGM user, on a particular output from a particular AMPPD job step 
 * (i.e. Galaxy workflow step), for a particular purpose. The status of a task goes through a finite set of changes as it's being worked on.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class HmgmTask extends Dataentity {
	
	public enum Type {TRANSCRIPT, NER, STRUCTURE};
	public enum Status {OPEN, READY, ASSIGNED, PROGRESS, REVIEW, COMPLETED};
	public enum Level {PASS, AVERAGE, DETAILED};

	// The following Galaxy ID fields uniquely identify the Galaxy dataset (an output file from a particular job)  to be worked on for this task	
	private String workflowId;
	private String invocationId;
	private String stepId;
	private String datasetId;
	
	// purpose of the task, could be: Transcript correction, NER output revision, Structural metadata creation
	private Type type;

	// degree of refinement required of the result, could be: pass-through, average, detailed
	private Level level;
	
	// current status, could be: Open, Ready (level of detail has been provided by supervisor), Assigned, In progress, Under review, Completed
	private Status status;

	// the path (relative to amppd root) for the output file saved while work is in progress
	// note that each time assignee saves the file it will overwrites the previous version
	// and when the task is submitted, this file becomes the final version sent to Galaxy HMGM tool
	private String outputFilepath;
	
	@OneToMany(mappedBy="hmgmTask")
	private Set<HmgmNote> hmgmNotes;
	
	@ManyToOne 
	private AmpUser assignedTo;

	/* Note: 
	 * In the future we could add a list of TaskAction to record the history of a task workflow; 
	 * however, for now we can acquire the history via DB auditing tools which is sufficient for internal use.
	 */
	
}
