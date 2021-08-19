package edu.indiana.dlib.amppd.model.unused;

import java.sql.Timestamp;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Job represents an execution of a workflow against a bag.
 * @author yingfeng
 *
 */
@Entity
@Data
@EqualsAndHashCode(exclude={"jobMgmModes", "primaryfile", "workflow"})
@ToString(exclude={"jobMgmModes", "primaryfile", "workflow"})
public class Job {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
            
    private String submittedBy;
    private String status;
    private String errorMessage;
    private Timestamp timeStarted;
    private Timestamp timeEnded;
    
    @OneToMany(mappedBy="job")
    private Set<JobMgmMode> jobMgmModes;
    
//    @ManyToOne
//    private Primaryfile primaryfile;

//    @ManyToOne
//    private InputBag bag;

    @ManyToOne
    private Workflow workflow;

}
