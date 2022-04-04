package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

/**
 * Information related to a workflow edit session by an authenticated AMP user on a particular workflow.
 * @author yingfeng
 */
@Entity
@Data
public class WorkflowEditSession {
	// TODO This class currently not in use, move to unused package
	
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

//	//@NotNull
//	@Index(unique="true")
//    @OneToOne(targetEntity = AmpUser.class, fetch = FetchType.EAGER)
//    @JoinColumn(nullable = false, name = "user_id")
//    private AmpUser user;
  
    //@NotNull	
    private String jwtToken;	// JWT token for the current authenticated user session

    //@NotNull	
    private String workflowId;  

    //@NotNull	
    private String galaxySession;  // galaxySession cookie value for workflow edit

    //@NotNull	
    private Date galaxyExpirationTime;  // galaxySession cookie expiration time
        
}
