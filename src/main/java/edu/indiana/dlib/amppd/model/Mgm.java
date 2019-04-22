package edu.indiana.dlib.amppd.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * Mgm defines the property related to a MGM tool. An MGM can have multiple modes, and is owned by a unit. 
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class Mgm extends Dataentity {
    
    private String version;
    private String platform;
    
//	private Long unitId;	
//	private Unit unit;
    
    @OneToMany(mappedBy="mgm")
    private List<MgmMode> modes;
        
}

