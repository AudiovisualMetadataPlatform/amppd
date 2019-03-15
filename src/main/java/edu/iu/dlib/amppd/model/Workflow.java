package edu.iu.dlib.amppd.model;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Item {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private String creator;
    private String owner;
    private Date dateCreated;
    
    private Date startMgmModeId;
    private Date endMgmModeId;

    private MgmMode startMgmMode;	
    private MgmMode endMgmMode;	
    private ArrayList<RouteLink> routeLiks
    
    
}

