package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Dataentity;


/**
 * Projection for a brief view of a data entity, suitable for listing entities.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Dataentity.class}) 
public interface DataentityBrief extends AmpObjectBrief {
	
	public String getName();
	public String getDescription();
	public Boolean getDeletable();
	
}
