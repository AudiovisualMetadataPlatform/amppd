package edu.iu.dlib.amppd.model;

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;

/**
 * RouteLink defines the from/to nodes as well as the correspondence between the outputs of the from node and the inputs of the to node in a workflow route graph. 
 * @author yingfeng
 *
 */
@Entity
@Data
public class RouteLink {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;    
    
    private HashMap<Integer, Integer> mgmModeIoMap;    
    
    // TODO double check the relationship
//    private Long fromMgmModeId;
    @ManyToOne
    private MgmMode fromMgmMode;

    // TODO double check the relationship
//    private Long toMgmModeId;   
    @ManyToOne
    private MgmMode toMgmMode;
    
//    private Long workflowId;
    @ManyToOne
    private Workflow workflow;

}

