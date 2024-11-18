package edu.indiana.dlib.amppd.model.unused;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.model.AmpObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

// TODO This class is not currently used.  
// Currently we assume that bundle is associated with primaryfiles directly, so we do not need a separate class InputBag for Inputs now.  
// If a workflow needs to have supplements to go with the primaryfile, we assume supplements associated with it at all levels will apply to the workflow. 

/**
 * InputBag represents the set of inputs to feed into a workflow. It contains one primaryfile and none or multiple supplement files,
 * which could be any combination of supplement files associated with the primaryfile, item, or collection
 * @author yingfeng
 *
 */
//@Entity
//@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded=true)
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class InputBag extends AmpObject {
	
//	  @ManyToOne
//    private Primaryfile primaryfile;	
//    
//    @ManyToMany(mappedBy = "bags")
//    private Set<Supplement> supplements;   
//    
//    @ManyToMany
//    private Set<Bundle> bundles;      
//    
//    @OneToMany(mappedBy="bag")
//    private Set<Job> jobs;        
  
	@JsonIgnore
	public Long getAcUnitId() {
		return null;
	}

 
}
