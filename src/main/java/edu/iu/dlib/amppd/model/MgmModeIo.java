package edu.iu.dlib.amppd.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class MgmModeIo {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    private Long mgmModeId;
    
    private String ioType;
    private Integer seqNo;
    
    // TODO the following fields need refinement, can be put into hashmap
    private String mimeType;
    private String syntacticTag; 
    private String semanticTag;

    private MgmMode mgmMode;

    
}

