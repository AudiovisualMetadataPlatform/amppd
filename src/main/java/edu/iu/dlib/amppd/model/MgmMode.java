package edu.iu.dlib.amppd.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import lombok.Data;

/**
 * MgmMode defines properties related to a mode of an MGM, as well as the inputs/outputs for that mode.
 * @author yingfeng
 *
 */
@Entity
@Data
public class MgmMode {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private String name;
    private String description;
    private HashMap<String, String> defaultParams;
    private String condition;	// TODO this can be a single String of compound boolean expression, or array of simple boolean expressions

    // TODO double check the relationship
    @OneToMany(mappedBy="mgmMode")
    private ArrayList<MgmModeInput> mgmModeInputs;
    
    // TODO double check the relationship
    @OneToMany(mappedBy="mgmMode")
    private ArrayList<MgmModeOutput> mgmModeOutputs;
    
    //  private Long mgmId;
    @ManyToOne
    private Mgm mgm;
  
    // TODO relations with routeLink, workflow?
}
