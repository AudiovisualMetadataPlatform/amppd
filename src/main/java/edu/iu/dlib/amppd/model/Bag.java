package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

/**
 * Bag represents the set of inputs to feed into a workflow. It contains one master file and none or multiple supplement files. 
 * @author yingfeng
 *
 */
@Entity
public class Bag extends Content {

//    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
//    private Long id;
//    
//    // TODO do we need these info or can they be represented through group/item
//    private String name;
//    private String description;
//    private String createdBy;
//    private Date dateCreated;

    private Long masterFileId;
    
    private MasterFile masterFile;	
    private ArrayList<SupplementFile> supplementFiles;    
    private ArrayList<Group> groups;  // TODO probably not needed        
}
