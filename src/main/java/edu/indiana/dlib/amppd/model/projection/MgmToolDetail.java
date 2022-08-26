package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmTool;
import edu.indiana.dlib.amppd.model.MgmVersion;

/**
 * Projection for a detailed view of an MgmTool.
 * @author yingfeng
 */
@Projection(name = "detail", types = {MgmTool.class}) 
public interface MgmToolDetail extends MgmToolBrief, MgmMetaDetail {
	
	public String getModule(); 
	public Set<MgmVersion> getVersions();
	
	@Value("#{target.category.id}")
	public String getCategoryId();
	
}
