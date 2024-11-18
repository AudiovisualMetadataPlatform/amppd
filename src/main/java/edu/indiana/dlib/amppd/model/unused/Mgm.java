package edu.indiana.dlib.amppd.model.unused;

import java.util.Set;

import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.model.AmpObject;
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
public class Mgm extends AmpObject {
    
    private String version;
    private String platform;    
    
    @OneToMany(mappedBy="mgm")
    private Set<MgmMode> modes;
        
	@JsonIgnore
    public Long getAcUnitId() {
    	return null;
    }

    
}

