package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

/**
 * Group is a container of one or multiple bags to which similar workflows can be applied.
 * @author yingfeng
 *
 */
@Entity
public class Group extends Content {

//    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
//    private Long id;
//    
//    private String name;
//    private String description;
//    private String createdBy;
//    private Date dateCreated;

    private ArrayList<Bag> bags;
    
}


