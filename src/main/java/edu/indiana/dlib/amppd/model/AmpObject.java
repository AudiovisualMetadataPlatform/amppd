package edu.indiana.dlib.amppd.model;

import java.util.Date;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Data;

/**
 * Base class for all AMP model classes, containing ID and database auditing information.
 * @author yingfeng
 *
 */
@MappedSuperclass
@Data
public abstract class AmpObject {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    @CreatedDate
    private Date createdDate;
 
    @LastModifiedDate
    private Date modifiedDate;
    
    @CreatedBy
    private String createdBy;
 
    @LastModifiedBy
    private String modifiedBy;    

    @Override
    public boolean equals(Object ampobj) {
    	return ampobj instanceof AmpObject && id.intValue() == (((AmpObject)ampobj).getId()).intValue();
    }
    
}
