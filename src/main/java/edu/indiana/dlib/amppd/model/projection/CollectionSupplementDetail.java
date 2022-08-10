package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.CollectionSupplement;

/**
 * Projection for a detailed view of a collection supplement, suitable for listing collection supplements.
 * @author yingfeng
 */
@Projection(name = "detail", types = {CollectionSupplement.class}) 
public interface CollectionSupplementDetail extends CollectionSupplementBrief, SupplementDetail {

	@Value("#{target.collection.id}")
	public String getCollectionId();
	
	@Value("#{target.collection.unit.id}")
	public String getUnitId();
	
}


