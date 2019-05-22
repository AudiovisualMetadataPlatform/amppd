package edu.indiana.dlib.amppd.model;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Workflow defines the directed graph with a single start and end node, where nodes represent MGMs, and links represent dependencies between MGMs. 
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class Workflow extends Dataentity {
    
	// TODO double check the relationship
	@ManyToOne
    private MgmMode startMgmMode;	
    
	// TODO double check the relationship   
	@ManyToOne
    private MgmMode endMgmMode;
    
    @OneToMany(mappedBy="workflow")
    private Set<RouteLink> routeLinks;    
    
    @OneToMany(mappedBy="workflow")
    private Set<Job> jobs;    

	// TODO: Unit & Workflow do not have a 1:M ownership relation, but could have a M:M access relation. When we add access control we shall reconsider this mapping 
//    @ManyToOne
//    private Unit unit;
    
}

