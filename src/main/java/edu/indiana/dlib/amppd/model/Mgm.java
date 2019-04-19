package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import lombok.Data;

/**
 * Mgm defines the property related to a MGM tool. An MGM can have multiple modes, and is owned by a unit. 
 * @author yingfeng
 *
 */
@Entity
@Data
public class Mgm extends AmpData {

    private String version;
    private String platform;
    
//	private Long unitId;	
//	private Unit unit;
    
    @OneToMany(mappedBy="mgm")
    private List<MgmMode> modes;
        
}

