package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmMeta;


/**
 * Projection for a brief view of an MGM meta object, suitable for listing.
 * @author yingfeng
 */
@Projection(name = "brief", types = {MgmMeta.class}) 
public interface MgmMetaBrief extends AmpObjectBrief {
	
	public String getName();
	public String getDescription();
	
}
