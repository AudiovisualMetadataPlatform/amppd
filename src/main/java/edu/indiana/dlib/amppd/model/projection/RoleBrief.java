package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.ac.Role;

/**
 * Projection for a brief view of a role.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Role.class}) 
public interface RoleBrief extends AmpObjectBrief {

	public String getName();
	public String getDescription();	
	public String getLevel();
	
	@Value("#{target.unit == null ? null : target.unit.id}")
	public Long getUnitId(); 

	@Value("#{target.unit == null ? null : target.unit.name}")
	public Long getUnitName(); 

}
