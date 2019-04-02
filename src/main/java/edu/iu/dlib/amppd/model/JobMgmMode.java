package edu.iu.dlib.amppd.model;

import java.net.URI;
import java.sql.Timestamp;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
    private Long jobId;
    private Long mgmModeId;
    
    private HashMap<String, String> params;
    private Double percentage;
    private String errorMessage;
    private Timestamp timeStarted;
    private Timestamp timeEnded;
    
    private HashMap<Integer, URI> mgmModeIoMap;
    
}
