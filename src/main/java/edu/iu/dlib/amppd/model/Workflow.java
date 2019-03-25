package edu.iu.dlib.amppd.model;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Entity;

/**
 * Workflow defines the directed graph with a single start and end node, where nodes represent MGMs, and links represent dependencies between MGMs. 
 * @author yingfeng
 *
 */
@Entity
public class Workflow extends Content {

//    @Id
//    @GeneratedValue(strategy=GenerationType.AUTO)
//    private Long id;
//    private String name;
//    private String description;
//    private String createdBy;
//    private Date dateCreated;
    
    private String unitId;
    private Long startMgmModeId;
    private Long endMgmModeId;
    
    private Unit unit;
    private MgmMode startMgmMode;	
    private MgmMode endMgmMode;	
    private ArrayList<RouteLink> routeLiks;    
    
}

