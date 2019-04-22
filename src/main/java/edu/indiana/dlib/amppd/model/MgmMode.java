package edu.indiana.dlib.amppd.model;

import java.util.HashMap;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * MgmMode defines properties related to a mode of an MGM, as well as the inputs/outputs for that mode.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class MgmMode extends Dataentity {
    
    private HashMap<String, String> defaultParams;
    private String condition;	// TODO this can be a single String of compound boolean expression, or array of simple boolean expressions

    // TODO double check the relationship
    @OneToMany(mappedBy="mgmMode")
    private List<MgmModeInput> mgmModeInputs;
    
    // TODO double check the relationship
    @OneToMany(mappedBy="mgmMode")
    private List<MgmModeOutput> mgmModeOutputs;
    
    //  private Long mgmId;
    @ManyToOne
    private Mgm mgm;
  
    // TODO relations with routeLink, workflow?
}
