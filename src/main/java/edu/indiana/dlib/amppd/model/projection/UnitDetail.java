package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Unit;


/**
 * Projection for a detailed view of a unit.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Unit.class}) 
public interface UnitDetail extends DataentityDetail {

	public Set<Collection> getCollections();
	
}
