package edu.indiana.dlib.amppd.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import lombok.Data;

/**
 * SupplementOfItem is a supplemental file associated with an item and shared by all primaryfiles within that item.
 * @author yingfeng
 *
 */
@Entity
@Data
public class SupplementOfItem extends Supplement {

//    private Long itemId;
	@ManyToOne
    private Item item;
    
}
