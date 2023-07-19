package edu.indiana.dlib.amppd.model.unused;

import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.model.Dataentity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Mgm defines the property related to a MGM tool. An MGM can have multiple modes, and is owned by a unit. 
 * @author yingfeng
 *
 */
//@Entity
//@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=true, exclude="modes")
@ToString(callSuper=true, exclude="modes")
public class Mgm extends Dataentity {
    
    private String version;
    private String platform;    
    
    @OneToMany(mappedBy="mgm")
    private Set<MgmMode> modes;
        
	@JsonIgnore
    public Long getAcUnitId() {
    	return null;
    }

    
}

