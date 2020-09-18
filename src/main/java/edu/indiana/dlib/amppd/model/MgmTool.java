package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.NonNull;

/**
 * This class contains information about the underlying MGM models used by MGM adapter tools in Galaxy.
 * Note that the table for this class is manually maintained, i.e. each time a new local MGM (or a new version of it) 
 * is installed, or a new version of the cloud MGM is released, this table shall be updated with that information. 
 * @author yingfeng
 *
 */
@EntityListeners(AuditingEntityListener.class)
@Entity
@Data
public class MgmTool {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    @NonNull
    private String toolId;	// ID of the MGM adapter tool in galaxy
    
    @NonNull
    private String mgmName;	// name of the underlying MGM model used by the adapter
        
    @NonNull
    private String version;	// version of the MGM model
    
    @NonNull
    private Date upgradeDate; // date when this version of the MGM model is installed (for local tools) or released (for cloud tools)

}
