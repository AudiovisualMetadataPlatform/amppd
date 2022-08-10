package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Unit;

/**
 * Projection for a brief view of a unit.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Unit.class}) 
public interface UnitBrief extends DataentityBrief {

}
