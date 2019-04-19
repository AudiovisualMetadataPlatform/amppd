package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Data;

/**
 * SupplementOfPrimaryfile is a supplemental file associated with a primaryfile and only available for this primaryfile.
 * @author yingfeng
 *
 */
@Entity
@Data
public class SupplementOfPrimaryfile extends Supplement {

//    private Long primaryId;
	@ManyToOne
    private Primaryfile primaryfile;
    
}
