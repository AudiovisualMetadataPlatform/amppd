package edu.indiana.dlib.amppd.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Super class for most data entities created in AMP. 
 * @author yingfeng
 *
 */
@MappedSuperclass
@Data
@NoArgsConstructor
public abstract class Dataentity {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    @NonNull
    private String name;
    
    @NonNull
    @Type(type="text")
//    @Column(columnDefinition="text")
//    @Column(length=2048)
    private String description;

    @CreatedDate
    private long createdDate;
 
    @LastModifiedDate
    private long modifiedDate;
    
    @CreatedBy
    private String createdBy;
 
    @LastModifiedBy
    private String modifiedBy;    

    // TODO: research LomBok issue: whenever no arg constructor exists (whether defined by code or by Lombok annotation) other constructors won't be added by Lombok despite the annotation
//    public Dataentity() {
//    	super();
//    }
//    
//    public Dataentity(String name, String description) {
//    	this.name = name;
//    	this.description = description;
//    }
    
}
