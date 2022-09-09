package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmTool;

/**
 * Projection for a brief view of an MgmTool.
 * @author yingfeng
 */
@Projection(name = "brief", types = {MgmTool.class}) 
public interface MgmToolBrief extends MgmMetaBrief {
	
	public String getToolId();
	public String getHelp();
	
}
