package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

/**
 * Bag represents the set of inputs to feed into a workflow. It contains one primary and none or multiple supplement files,
 * which could be any combination of supplement files associated with the primary, item, or collection
 * @author yingfeng
 *
 */
@Entity
@Data
public class Bag extends Dataentity {

//    private Long primaryId;	
	@ManyToOne
    private Primary primary;	
    
    @ManyToMany(mappedBy = "bags")
    private List<Supplement> supplements;   
    
    @ManyToMany
    private List<Group> groups;      
    
    @OneToMany(mappedBy="bag")
    private List<Job> jobs;    
}
