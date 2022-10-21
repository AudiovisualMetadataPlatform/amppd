package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.WorkflowResult;


/**
 * Projection for a brief view of a primaryfile.
 * @author yingfeng
 */
@Projection(name = "primaryfileIdName", types = {WorkflowResult.class}) 
public interface PrimaryfileIdName {
	
	public String getPrimaryfileId();	
	public String getPrimaryfileName();	
	public String getItemName();
	public String getCollectionName();	
	
}
