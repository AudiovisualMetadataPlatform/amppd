package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Item;


/**
 * Projection for a detailed view of an item.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Item.class}) 
public interface ItemDetail extends ItemBrief, ContentDetail {

	public Set<PrimaryfileBrief> getPrimaryfiles();
	public Set<ItemSupplementBrief> getSupplements();
	
}