package edu.iu.dlib.amppd.model;


import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Entity;

/**
 * Item represents an intellectual object that contains one or more master files and none or multiple supplement files.
 * @author yingfeng
 *
 */
@Entity
public class Item extends Content {

//    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
//    private Long id;
//    
//    private String name;
//    private String description;
//    private String createdBy;
//    private Date dateCreated;

    private Long collectionId;
    HashMap<String, String> externalIds;
    
    private Collection collection;	
    private ArrayList<MasterFile> masterFiles;
    private ArrayList<SupplementFile> supplementFiles;
        
}
