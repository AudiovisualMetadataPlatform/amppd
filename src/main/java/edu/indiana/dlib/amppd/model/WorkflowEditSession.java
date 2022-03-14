package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.jdo.annotations.Index;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import lombok.Data;

/**
 * Information related to a workflow edit session by an authenticated AMP user on a particular workflow.
 * @author yingfeng
 */
@Entity
@Data
public class WorkflowEditSession {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

	//@NotNull
	@Index(unique="true")
    @OneToOne(targetEntity = AmpUser.class, fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id")
    private AmpUser user;
  
	//@NotNull	
    private String workflowId;  

    //@NotNull	
    private String galaxySession;  

    //@NotNull	
    private Date galaxyExpires;
        
}
