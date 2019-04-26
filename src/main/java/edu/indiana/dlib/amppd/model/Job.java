package edu.indiana.dlib.amppd.model;

import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

/**
 * Job represents an execution of a workflow against a bag.
 * @author yingfeng
 *
 */
@Entity
@Data
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
    private List<JobMgmMode> jobMgmModes;
    
    @ManyToOne
    private Bag bag;

    @ManyToOne
    private Workflow workflow;

}
