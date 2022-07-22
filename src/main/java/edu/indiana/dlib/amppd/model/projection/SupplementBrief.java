package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Supplement;

/**
 * Projection for a brief view of a supplement, suitable for listing supplements.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Supplement.class}) 
public interface SupplementBrief extends DataentityBrief {
	
	public String getCategory();	

}
