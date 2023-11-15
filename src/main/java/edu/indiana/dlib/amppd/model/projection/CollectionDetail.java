package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Collection;


/**
 * Projection for a detailed view of a collection.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Collection.class}) 
public interface CollectionDetail extends CollectionBrief, ContentDetail {

	public Set<ItemBrief> getItems();
	public Set<CollectionSupplementBrief> getSupplements();
	
}
