package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * MgmModeIo defines properties related to an input/output of an MGM mode.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
//@DiscriminatorColumn(name = "type")
public abstract class MgmModeIo extends Dataentity {
   
//    private String ioType;
    private Integer seqNo;
    
    // TODO the following fields need refinement, can be put into hashmap
    private String mimeType;
    private String syntacticTag; 
    private String semanticTag;

    // TODO double check the relationship
//    private Long mgmModeId;
    @ManyToOne
    private MgmMode mgmMode;
    
}

