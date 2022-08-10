package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Collection;

/**
 * Projection for a brief view of a collection.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Collection.class}) 
public interface CollectionBrief extends ContentBrief {

	public Boolean getActive();	
	public String getTaskManager();
	
	@Value("#{target.unit.name}")
	public String getUnitName();
	
}
