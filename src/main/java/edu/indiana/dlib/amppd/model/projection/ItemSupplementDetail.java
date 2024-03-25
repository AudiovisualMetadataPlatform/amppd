package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.ItemSupplement;

/**
 * Projection for a detailed view of a item supplement, suitable for listing item supplements.
 * @author yingfeng
 */
@Projection(name = "detail", types = {ItemSupplement.class}) 
public interface ItemSupplementDetail extends ItemSupplementBrief, SupplementDetail {
	
}

