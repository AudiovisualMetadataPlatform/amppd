package edu.iu.dlib.amppd.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Job represents an execution of a workflow against a bag.
 * @author yingfeng
 *
 */
@Entity
public class Job {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long bagId;
    private Long workflowId;
        
    private String submittedBy;
    private String status;
    private String errorMessage;
    private Timestamp timeStarted;
    private Timestamp timeEnded;
    
    private Bag bag;
    private Workflow workflow;

}
