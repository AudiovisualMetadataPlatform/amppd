package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Supplement;

/**
 * Projection for a detailed view of an item.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Supplement.class}) 
public interface SupplementDetail extends AssetDetail {

	public String getCategory();	

}

