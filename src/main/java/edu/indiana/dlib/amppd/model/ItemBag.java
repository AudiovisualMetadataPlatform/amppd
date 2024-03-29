package edu.indiana.dlib.amppd.model;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * ItemBag is the output bag for an item containing all the PrimaryfileBags associated with the primaryfiles contained in the item.
 * @author yingfeng
 */
@Data
public class ItemBag {
	private Long itemId;
	private String itemName;
    private String externalSource;
    private String externalId;

    @EqualsAndHashCode.Exclude
	@ToString.Exclude
    private List<PrimaryfileBag> primaryfileBags;
}
