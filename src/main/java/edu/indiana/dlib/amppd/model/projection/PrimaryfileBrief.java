package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Primaryfile;

/**
 * Projection for a brief view of a primaryfile.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Primaryfile.class}) 
public interface PrimaryfileBrief extends AssetBrief {
	
	@Value("#{target.item.name}")
	public String getItemName();
	
	@Value("#{target.item.collection.name}")
	public String getCollectionName();	
	
	@Value("#{target.item.collection.unit.name}")
	public String getUnitName();
	
	@Value("#{target.item.id}")
	public String getItemId();
	
	@Value("#{target.item.collection.id}")
	public String getCollectionId();	
	
	@Value("#{target.item.collection.unit.id}")
	public String getUnitId();
	
}
