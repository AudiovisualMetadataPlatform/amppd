package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Dataentity;


/**
 * Projection for brief view of all data entities.
 * @author yingfeng
 */
@Projection(name = "brief", types = { Dataentity.class }) 
public interface DataentityBrief {
	
	public String getName();
	public String getDescription();
	
}
