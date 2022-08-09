package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Supplement;

/**
 * Projection for a detailed view of a supplement, suitable for displaying individual supplement.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Supplement.class}) 
public interface SupplementDetail extends SupplementBrief, AssetDetail {

}

