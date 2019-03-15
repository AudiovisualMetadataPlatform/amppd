package edu.iu.dlib.amppd.model;

import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Mgm {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long collectionId;
    private Long groupId;
    
    private String name;
    private String description;
    private String version;
    private String platform;
    private String owner;
    private Date registrationDate;

    private ArrayList<MgmMode> modes;
    
    
}

