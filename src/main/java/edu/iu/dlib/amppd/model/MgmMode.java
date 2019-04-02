package edu.iu.dlib.amppd.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
    private Long mgmId;
    
    private String name;
    private String description;
    private HashMap<String, String> defaultParams;
    private String condition;	// TODO this can be a single String of compound boolean expression, or array of simple boolean expressions

    private Mgm mgm;
    private ArrayList<MgmModeIo> mgmModeIoInputs;
    private ArrayList<MgmModeIo> mgmModeIoOutputs;
    
}
