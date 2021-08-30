package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;


/**
 * Projection for a detailed view of a collection.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Collection.class}) 
public interface CollectionDetail extends ContentDetail {

	public Set<Item> getItems();
	public Set<CollectionSupplement> getSupplements();
	
}
