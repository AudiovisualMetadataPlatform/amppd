package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.ItemSupplement;

/**
 * Projection for a brief view of a item supplement, suitable for listing item supplements.
 * @author yingfeng
 */
@Projection(name = "brief", types = {ItemSupplement.class}) 
public interface ItemSupplementBrief extends SupplementBrief {

	@Value("#{target.item.name}")
	public String getItemName();
	
	@Value("#{target.item.collection.name}")
	public String getCollectionName();	
	
	@Value("#{target.item.collection.unit.name}")
	public String getUnitName();
	
	@Value("#{target.item.id}")
	public String getItemId();
	
	@Value("#{target.item.collection.id}")
	public String getCollectionId();	
	
	@Value("#{target.item.collection.unit.id}")
	public String getUnitId();
	
}
