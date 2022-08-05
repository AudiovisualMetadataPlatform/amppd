package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Index;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This class contains version/upgrade information about the underlying MGM model used by an MGM adapter in Galaxy.
 * Note that the table for this class is manually maintained, i.e. each time a new local MGM (or a new version of it) 
 * is installed, or a new version of the cloud MGM is released, this table shall be updated with that information. 
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = {
		@Index(columnList = "mgm_id"),
		@Index(columnList = "upgradeDate"),
		@Index(columnList = "mgm_id, upgradeDate", unique = true)
})
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
public class MgmVersion extends AmpObject {	
        
    // version of the module/package/model (not MGM adapter version in Galaxy) 
	@NotNull
    private String version;	
    
    // date when this version of the MGM module is installed (for local tools) or released (for cloud tools)
	@NotNull
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss.SSS")
    private Date upgradeDate; 

    // reference to the parent MGM of this version instance
	@NotNull
	@ManyToOne
    private MgmTool mgm; 

}
