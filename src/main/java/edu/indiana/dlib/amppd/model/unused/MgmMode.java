package edu.indiana.dlib.amppd.model.unused;

import java.util.HashMap;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import edu.indiana.dlib.amppd.model.Dataentity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * MgmMode defines properties related to a mode of an MGM, as well as the inputs/outputs for that mode.
 * @author yingfeng
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=true, exclude={"mgmModeInputs", "mgmModeOutputs", "mgm"})
@ToString(callSuper=true, exclude= {"mgmModeInputs", "mgmModeOutputs", "mgm"})
public class MgmMode extends Dataentity {
    
    private HashMap<String, String> defaultParams;
    private String condition;	// TODO this can be a single String of compound boolean expression, or array of simple boolean expressions

    // TODO double check the relationship
    @OneToMany(mappedBy="mgmMode")
    private Set<MgmModeInput> mgmModeInputs;
    
    // TODO double check the relationship
    @OneToMany(mappedBy="mgmMode")
    private Set<MgmModeOutput> mgmModeOutputs;
    
    @ManyToOne
    private Mgm mgm;
  
    // TODO relations with routeLink, workflow?
}
