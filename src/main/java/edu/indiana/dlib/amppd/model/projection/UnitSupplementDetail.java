package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.UnitSupplement;

/**
 * Projection for a detailed view of a unit supplement, suitable for listing unit supplements.
 * @author yingfeng
 */
@Projection(name = "detail", types = {UnitSupplement.class}) 
public interface UnitSupplementDetail extends UnitSupplementBrief, SupplementDetail {

	@Value("#{target.unit.id}")
	public String getUnitId();
	
}
