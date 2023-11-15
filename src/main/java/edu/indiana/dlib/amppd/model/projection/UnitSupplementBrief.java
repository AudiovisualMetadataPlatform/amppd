package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.UnitSupplement;

/**
 * Projection for a brief view of a unit supplement, suitable for listing unit supplements.
 * @author yingfeng
 */
@Projection(name = "brief", types = {UnitSupplement.class}) 
public interface UnitSupplementBrief extends SupplementBrief {

	@Value("#{target.unit.name}")
	public String getUnitName();
	
	@Value("#{target.unit.id}")
	public String getUnitId();
	
}
