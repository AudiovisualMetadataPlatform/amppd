package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmCategory;

/**
 * Projection for a brief view of an MgmCategory.
 * @author yingfeng
 */
@Projection(name = "brief", types = {MgmCategory.class}) 
public interface MgmCategoryBrief extends MgmMetaBrief {

	public String getSectionId();
	public String getHelp();

	@Value("#{target.msts.size}")
	public String getMstsCount();
	
}
