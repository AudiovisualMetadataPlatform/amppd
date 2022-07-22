package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.CollectionSupplement;

/**
 * Projection for a brief view of a collection supplement, suitable for listing collection supplements.
 * @author yingfeng
 */
@Projection(name = "brief", types = {CollectionSupplement.class}) 
public interface CollectionSupplementBrief extends SupplementBrief {

	@Value("{target.collection.name}")
	public String getCollectionName();
	
}
