package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

// TODO this class can be removed at some point
// Currently we assume that bundle is associated with item (and thus all the primaryfiles belonging to that item) directly. 
// We will not use the concept of Bag for now. If a workflow needs to have supplements to go with the primaryfile, 
// we assume supplements associated with it at all levels will apply to the workflow. 

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
	
//	  @ManyToOne
//    private Primaryfile primaryfile;	
//    
//    @ManyToMany(mappedBy = "bags")
//    private List<Supplement> supplements;   
//    
//    @ManyToMany
//    private List<Bundle> bundles;      
//    
//    @OneToMany(mappedBy="bag")
//    private List<Job> jobs;        
    
}
