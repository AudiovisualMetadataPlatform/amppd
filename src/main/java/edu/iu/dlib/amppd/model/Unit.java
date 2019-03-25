package edu.iu.dlib.amppd.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Entity;

/**
 * Organization unit that owns collections and workflows.
 * @author yingfeng
 *
 */
@Entity
public class Unit extends Content {

//    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
//    private Long id;
//    
//    private String name;
//    private String description;
//    private String createdBy;
//    private Date dateCreated;
    
	HashMap<String, String> externalIds;
	
    // TODO may not need these
    private ArrayList<Collection> collections;
    private ArrayList<Workflow> workflows;
}
