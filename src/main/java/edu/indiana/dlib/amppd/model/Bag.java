package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Bag represents the set of inputs to feed into a workflow. It contains one primaryfile and none or multiple supplement files,
 * which could be any combination of supplement files associated with the primaryfile, item, or collection
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class Bag extends Dataentity {
	
	@ManyToOne
    private Primaryfile primaryfile;	
    
    @ManyToMany(mappedBy = "bags")
    private List<Supplement> supplements;   
    
    @ManyToMany
    private List<Group> groups;      
    
    @OneToMany(mappedBy="bag")
    private List<Job> jobs;        
    
}
