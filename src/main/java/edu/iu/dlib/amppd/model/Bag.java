package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bag represents the set of inputs to feed into a workflow. It contains one masterfile and none or multiple supplement files. 
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class Bag extends Data {

    private Long masterfileId;	
    private Masterfile masterfile;	
    
    @ManyToMany(mappedBy = "bags")
    private ArrayList<Supplement> supplements;    // could be any combination of supplement files associated with the masterfile, item, or collection
    
    @ManyToMany(mappedBy = "bags")
    private ArrayList<Group> groups;      
    
}
