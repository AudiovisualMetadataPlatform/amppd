package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

import lombok.Data;

/**
 * Workflow defines the directed graph with a single start and end node, where nodes represent MGMs, and links represent dependencies between MGMs. 
 * @author yingfeng
 *
 */
@Entity
@Data
public class Workflow extends AmpData {
    
    private String unitId;
    private Long startMgmModeId;
    private Long endMgmModeId;
    
    private Unit unit;
    private MgmMode startMgmMode;	
    private MgmMode endMgmMode;	
    private ArrayList<RouteLink> routeLiks;    
    
}

