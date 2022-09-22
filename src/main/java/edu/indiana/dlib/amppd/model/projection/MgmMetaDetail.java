package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.MgmMeta;


/**
 * Projection for a detailed view of an MGM meta object, suitable for displaying individuals.
 * @author yingfeng
 */
@Projection(name = "detail", types = {MgmMeta.class}) 
public interface MgmMetaDetail extends MgmMetaBrief, AmpObjectBrief {
	
}