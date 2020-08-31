package edu.indiana.dlib.amppd.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * CollectionBag is OutputBag for Collection including all ItemBags associated with the items contained in the Collection.
 * @author yingfeng
 */
@Data
@EqualsAndHashCode
@ToString(callSuper=true, onlyExplicitlyIncluded=true)
public class CollectionBag {
	private Long id;				// id of the Collection
    List<ItemBag> itemBags;
}
