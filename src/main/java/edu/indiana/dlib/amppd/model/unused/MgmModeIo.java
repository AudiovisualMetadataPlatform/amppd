package edu.indiana.dlib.amppd.model.unused;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import edu.indiana.dlib.amppd.model.Dataentity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * MgmModeIo defines properties related to an input/output of an MGM mode.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@EqualsAndHashCode(callSuper=true, exclude="mgmMode")
@ToString(callSuper=true, exclude="mgmMode")
public abstract class MgmModeIo extends Dataentity {
   
    private Integer seqNo;
    
    // TODO the following fields need refinement, can be put into hashmap
    private String mimeType;
    private String syntacticTag; 
    private String semanticTag;

    // TODO double check the relationship
    @ManyToOne
    private MgmMode mgmMode;
    
}

