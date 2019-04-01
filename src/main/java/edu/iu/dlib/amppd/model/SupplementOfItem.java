package edu.iu.dlib.amppd.model;

import javax.persistence.Entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * SupplementOfItem is a supplemental file associated with an item and shared by all masterfiles within that item.
 * @author yingfeng
 *
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class SupplementOfItem extends Supplement {

    private Long itemId;
    private Item item;
    
}
