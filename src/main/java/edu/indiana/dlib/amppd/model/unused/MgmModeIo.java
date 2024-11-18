package edu.indiana.dlib.amppd.model.unused;

import javax.persistence.ManyToOne;

import edu.indiana.dlib.amppd.model.AmpObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * MgmModeIo defines properties related to an input/output of an MGM mode.
 * @author yingfeng
 *
 */
//@Entity
//@EntityListeners(AuditingEntityListener.class)
//@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Data
@EqualsAndHashCode(callSuper=true, exclude="mgmMode")
@ToString(callSuper=true, exclude="mgmMode")
public abstract class MgmModeIo extends AmpObject {
   
    private Integer seqNo;
    
    // TODO the following fields need refinement, can be put into hashmap
    private String mimeType;
    private String syntacticTag; 
    private String semanticTag;

    // TODO double check the relationship
    @ManyToOne
    private MgmMode mgmMode;
    
}

