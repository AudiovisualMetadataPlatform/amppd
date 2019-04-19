package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import lombok.Data;

/**
 * Workflow defines the directed graph with a single start and end node, where nodes represent MGMs, and links represent dependencies between MGMs. 
 * @author yingfeng
 *
 */
@Entity
@Data
public class Workflow extends Dataentity {
    
	// TODO double check the relationship
//    private Long startMgmModeId;
	@ManyToOne
    private MgmMode startMgmMode;	
    
	// TODO double check the relationship
//    private Long endMgmModeId;    
	@ManyToOne
    private MgmMode endMgmMode;
    
    @OneToMany(mappedBy="workflow")
    private List<RouteLink> routeLinks;    
    
    @OneToMany(mappedBy="workflow")
    private List<Job> jobs;    
    
    //  private String unitId;
    @ManyToOne
    private Unit unit;
    
}

