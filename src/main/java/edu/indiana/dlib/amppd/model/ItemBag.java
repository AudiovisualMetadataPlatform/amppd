package edu.indiana.dlib.amppd.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * ItemBag is OutputBag for Item including all BagContents associated with the primaryfiles contained in the item.
 * @author yingfeng
 */
@Data
@EqualsAndHashCode
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class ItemBag {
	private Long id;				// id of the item
    private String externalId;
    private String externalSource;
    List<BagContent> bagContents;
}
