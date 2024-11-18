package edu.indiana.dlib.amppd.model.unused;

import java.util.HashMap;
import java.util.Set;

import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.indiana.dlib.amppd.model.AmpObject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * MgmMode defines properties related to a mode of an MGM, as well as the inputs/outputs for that mode.
 * @author yingfeng
 *
 */
//@Entity
//@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=true, exclude={"mgmModeInputs", "mgmModeOutputs", "mgm"})
@ToString(callSuper=true, exclude= {"mgmModeInputs", "mgmModeOutputs", "mgm"})
public class MgmMode extends AmpObject {
    
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
    
	@JsonIgnore
    public Long getAcUnitId() {
    	return null;
    }


}
