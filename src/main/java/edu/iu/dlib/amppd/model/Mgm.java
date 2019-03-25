package edu.iu.dlib.amppd.model;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Entity;

/**
 * Mgm defines the property related to a MGM tool. An MGM can have multiple modes, and is owned by a unit. 
 * @author yingfeng
 *
 */
@Entity
public class Mgm extends Content {

//    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
//    private Long id;
//    private String name;
//    private String description;
//    private Date registrationDate;

	private Long unitId;
    private String version;
    private String platform;

	private Unit unit;
    private ArrayList<MgmMode> modes;
        
}

