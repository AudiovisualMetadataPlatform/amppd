package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.MgmScoringTool;

/**
 * Projection for a detailed view of an MgmCategory.
 * @author yingfeng
 */
@Projection(name = "detail", types = {MgmCategory.class}) 
public interface MgmCategoryDetail extends AmpObjectDetail {

	public String getName();	
	public String getSectionId();
	public String getDescription();
	public Set<MgmScoringTool> getMsts();
	
}
