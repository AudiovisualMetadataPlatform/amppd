package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SupplementOfMasterfile is a supplemental file associated with a masterfile and only available for this masterfile.
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class SupplementOfMasterfile extends Supplement {

    private Long masterfileId;	
    private Masterfile masterfile;
    
}
