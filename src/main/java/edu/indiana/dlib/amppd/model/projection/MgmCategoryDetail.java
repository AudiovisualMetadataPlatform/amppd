package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmCategory;

/**
 * Projection for a detailed view of an MgmCategory.
 * @author yingfeng
 */
@Projection(name = "detail", types = {MgmCategory.class}) 
public interface MgmCategoryDetail extends MgmCategoryBrief, MgmMetaDetail {

	public Set<MgmScoringToolBrief> getMsts();
	public Set<MgmToolBrief> getMgms();

}
