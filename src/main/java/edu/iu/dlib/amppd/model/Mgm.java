package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

import lombok.Data;

/**
 * Mgm defines the property related to a MGM tool. An MGM can have multiple modes, and is owned by a unit. 
 * @author yingfeng
 *
 */
@Entity
@Data
public class Mgm extends BO {

    private String version;
    private String platform;
	private Long unitId;
	
	private Unit unit;
    private ArrayList<MgmMode> modes;
        
}

