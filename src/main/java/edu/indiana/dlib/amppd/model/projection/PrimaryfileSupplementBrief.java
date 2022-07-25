package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;

/**
 * Projection for a brief view of a primaryfile supplement, suitable for listing primaryfile supplements.
 * @author yingfeng
 */
@Projection(name = "brief", types = {PrimaryfileSupplement.class}) 
public interface PrimaryfileSupplementBrief extends SupplementBrief {

	@Value("{target.primaryfile.name}")
	public String getPrimaryfileName();
	
	@Value("{target.primaryfile.item.name}")
	public String getItemName();
	
	@Value("{target.primaryfile.item.collection.name}")
	public String getCollectionName();	
	
	@Value("{target.primaryfile.item.collection.unit.name}")
	public String getUnitName();
		
}

