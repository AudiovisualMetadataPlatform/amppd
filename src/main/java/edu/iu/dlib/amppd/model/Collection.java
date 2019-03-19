package edu.iu.dlib.amppd.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Entity;

/**
 * Collection contains one or more items, and belongs to one and only one unit.
 * @author yingfeng
 *
 */
@Entity
public class Collection extends Content {

//    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
//    private Long id;
//    
//    private String name;
//    private String description;
//    private String createdBy;
//    private Date dateCreated;

    private Long unitId;
	HashMap<String, String> externalIds;

	private Unit unit;
    private ArrayList<Item> items; // TODO may not need this
}
