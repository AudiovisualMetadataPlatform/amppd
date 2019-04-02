package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;

import lombok.Data;

/**
 * SupplementOfItem is a supplemental file associated with an item and shared by all masterfiles within that item.
 * @author yingfeng
 *
 */
@Entity
@Data
public class SupplementOfItem extends Supplement {

    private Long itemId;
    private Item item;
    
}
