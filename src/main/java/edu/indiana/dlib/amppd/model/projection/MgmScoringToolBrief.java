package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmScoringTool;


/**
 * Projection for a brief view of an MgmScoringTool.
 * @author yingfeng
 */
@Projection(name = "brief", types = {MgmScoringTool.class}) 
public interface MgmScoringToolBrief extends MgmMetaBrief {
	
	public String getToolId();
	public String getGroundtruthTemplate();
	
}
