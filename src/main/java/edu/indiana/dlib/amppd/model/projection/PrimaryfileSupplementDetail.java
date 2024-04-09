package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;

/**
 * Projection for a detailed view of a primaryfile supplement, suitable for listing primaryfile supplements.
 * @author yingfeng
 */
@Projection(name = "detail", types = {PrimaryfileSupplement.class}) 
public interface PrimaryfileSupplementDetail extends PrimaryfileSupplementBrief, SupplementDetail {

}


