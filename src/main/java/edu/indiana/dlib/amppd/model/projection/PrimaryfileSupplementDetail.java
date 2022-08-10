package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;

/**
 * Projection for a detailed view of a primaryfile supplement, suitable for listing primaryfile supplements.
 * @author yingfeng
 */
@Projection(name = "detail", types = {PrimaryfileSupplement.class}) 
public interface PrimaryfileSupplementDetail extends SupplementBrief {

	@Value("#{target.primaryfile.id}")
	public String getPrimaryfileId();
	
	@Value("#{target.primaryfile.item.id}")
	public String getItemId();
	
	@Value("#{target.primaryfile.item.collection.id}")
	public String getCollectionId();	
	
	@Value("#{target.primaryfile.item.collection.unit.id}")
	public String getUnitId();
		
}


