package edu.iu.dlib.amppd.model;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import lombok.Data;

/**
 * MgmModeIo defines properties related to an input/output of an MGM mode.
 * @author yingfeng
 *
 */
@Entity
@Data
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type")
public abstract class MgmModeIo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
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

