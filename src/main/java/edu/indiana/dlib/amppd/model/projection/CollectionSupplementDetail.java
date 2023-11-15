package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.CollectionSupplement;

/**
 * Projection for a detailed view of a collection supplement, suitable for listing collection supplements.
 * @author yingfeng
 */
@Projection(name = "detail", types = {CollectionSupplement.class}) 
public interface CollectionSupplementDetail extends CollectionSupplementBrief, SupplementDetail {
	
}


