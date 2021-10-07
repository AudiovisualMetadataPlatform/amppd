package edu.indiana.dlib.amppd.model.unused;

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * RouteLink defines the from/to nodes as well as the correspondence between the outputs of the from node and the inputs of the to node in a workflow route graph. 
 * @author yingfeng
 *
 */
@Entity
@Data
@EqualsAndHashCode(exclude={"fromMgmMode", "toMgmMode", "workflow"})
@ToString(exclude={"fromMgmMode", "toMgmMode", "workflow"})
public class RouteLink {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;    
    
    private HashMap<Integer, Integer> mgmModeIoMap;    
    
    // TODO double check the relationship
    @ManyToOne
    private MgmMode fromMgmMode;

    // TODO double check the relationship 
    @ManyToOne
    private MgmMode toMgmMode;
    
    @ManyToOne
    private Workflow workflow;

}

