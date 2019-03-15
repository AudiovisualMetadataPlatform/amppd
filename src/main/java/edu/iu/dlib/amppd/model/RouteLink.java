package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class RouteLink {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;    
    private Long fromMgmModeId;
    private Long toMgmModeId;
    private HashMap<Integer, Integer> mgmModeIoMap;
    
    private MgmMode fromMgmMode;
    private MgmMode toMgmMode;
    
}

