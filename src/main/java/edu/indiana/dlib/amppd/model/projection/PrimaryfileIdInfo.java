package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.WorkflowResult;


/**
 * Projection for a brief view of a primaryfile.
 * @author yingfeng
 */
@Projection(name = "primaryfileId", types = {WorkflowResult.class}) 
public interface PrimaryfileIdInfo {
	
	public Long getUnitId();
//	public String getUnitName();	
	public Long getCollectionId();
//	public String getCollectionName();	
	public Long getItemId();
//	public String getItemName();
	public Long getPrimaryfileId();	
//	public String getPrimaryfileName();	
	
}
