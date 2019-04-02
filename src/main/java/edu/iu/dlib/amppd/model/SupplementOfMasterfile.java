package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;

import lombok.Data;

/**
 * SupplementOfMasterfile is a supplemental file associated with a masterfile and only available for this masterfile.
 * @author yingfeng
 *
 */
@Entity
@Data
public class SupplementOfMasterfile extends Supplement {

    private Long masterfileId;	
    private Masterfile masterfile;
    
}
