package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Data;

/**
 * SupplementOfCollection is a supplemental file associated with a collection and shared by all items within that collection.
 * @author yingfeng
 *
 */
@Entity
@Data
public class SupplementOfCollection extends Supplement {

//    private Long collectionId;
	@ManyToOne
    private Collection collection;
    
}
