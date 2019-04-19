package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Data;

/**
 * SupplementOfPrimary is a supplemental file associated with a primary and only available for this primary.
 * @author yingfeng
 *
 */
@Entity
@Data
public class SupplementOfPrimary extends Supplement {

//    private Long primaryId;
	@ManyToOne
    private Primary primary;
    
}
