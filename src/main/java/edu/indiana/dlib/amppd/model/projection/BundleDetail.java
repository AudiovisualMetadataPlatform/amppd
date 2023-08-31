package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Bundle;


/**
 * Projection for a detailed view of a bundle.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Bundle.class}) 
public interface BundleDetail extends BundleBrief, DataentityDetail {

	public Set<PrimaryfileBrief> getPrimaryfiles();
	
}
