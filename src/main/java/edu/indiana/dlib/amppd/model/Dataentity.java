package edu.indiana.dlib.amppd.model;

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
 * Super class for most data entities created in AMP. 
 * @author yingfeng
 *
 */
@MappedSuperclass
@Data
public abstract class Dataentity {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private String name;
    private String description;

    @CreatedDate
    private long createdDate;
 
    @LastModifiedDate
    private long modifiedDate;
    
    @CreatedBy
    private String createdBy;
 
    @LastModifiedBy
    private String modifiedBy;    

//    public Dataentity() {
//    	super();
//    }
//    
//    public Dataentity(String name, String description) {
//    	this.name = name;
//    	this.description = description;
//    }
    
}
