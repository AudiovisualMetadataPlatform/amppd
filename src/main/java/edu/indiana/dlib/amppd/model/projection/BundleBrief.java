package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Bundle;


/**
 * Projection for a brief view of a bundle.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Bundle.class}) 
public interface BundleBrief extends DataentityBrief {

}


