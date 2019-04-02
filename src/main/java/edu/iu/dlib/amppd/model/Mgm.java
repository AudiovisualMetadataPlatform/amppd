package edu.iu.dlib.amppd.model;

import java.util.ArrayList;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Mgm defines the property related to a MGM tool. An MGM can have multiple modes, and is owned by a unit. 
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class Mgm extends Data {

    private String version;
    private String platform;
	private Long unitId;
	
	private Unit unit;
    private ArrayList<MgmMode> modes;
        
}

