package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmTool;
import edu.indiana.dlib.amppd.model.MgmVersion;

/**
 * Projection for a detailed view of an MgmTool.
 * @author yingfeng
 */
@Projection(name = "detail", types = {MgmTool.class}) 
public interface MgmToolDetail extends AmpObjectDetail {
	
	public String getToolId();
	public String getName();	
	public String getHelp();
	public String getModule(); 
	public Set<MgmVersion> getVersionss();
	
}
