package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.AmpObject;

/**
 * Projection for a brief view of an AMP objects, suitable for listing objects.
 * @author yingfeng
 */
@Projection(name = "brief", types = {AmpObject.class}) 
public interface AmpObjectBrief {

	public Long getId();
	
}
