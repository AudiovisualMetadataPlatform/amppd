package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Dataentity;

/**
 * Projection for a detailed view of a data entity, suitable for displaying individual entity.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Dataentity.class}) 
public interface DataentityDetail extends AmpObjectDetail, DataentityBrief {  

}
