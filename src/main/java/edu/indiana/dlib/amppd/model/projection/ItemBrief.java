package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Item;

/**
 * Projection for a brief view of an item.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Item.class}) 
public interface ItemBrief extends ContentBrief {
		
	@Value("#{target.collection.name}")
	public String getCollectionName();	
	
	@Value("#{target.collection.unit.name}")
	public String getUnitName();
	
}

