package edu.indiana.dlib.amppd.model;

import java.net.URI;
import java.sql.Timestamp;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

/**
 * JobMgmMode represents the status and the inputs/outputs of an MGM mode during a job execution.
 * @author yingfeng
 *
 */
@Entity
@Data
public class JobMgmMode {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private HashMap<String, String> params;
    private Double percentage;	// value range is 0..100
    private String errorMessage;
    private Timestamp timeStarted;
    private Timestamp timeEnded;
    
    // TODO: the output could be a file or JSON blob; we might need a separate entity to represent this, 
    // as we need to add "final" flag; also, for file relative path not URI shall be stored. 
    private HashMap<Integer, URI> mgmModeIoMap;
    
    @ManyToOne
    private Job job;
    
    @ManyToOne
    private MgmMode mgmMode;

    // TODO might need to rephrase the status constants
    public String getStatus() {
    	if (percentage == 0.0 )
    		return "WAITING";
    	else if (errorMessage != null)
    		return "ERROR";
    	else if (percentage > 0 )
    		return "PROCESSING";
    	else if (percentage >= 100.0 )
    		return "cOMPLETED";
    	return null;
    }
    	    
}
