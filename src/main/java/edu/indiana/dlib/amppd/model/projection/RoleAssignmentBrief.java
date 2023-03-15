package edu.indiana.dlib.amppd.model.projection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.ac.RoleAssignment;

/**
 * Projection for a brief view of a roleAssignment.
 * @author yingfeng
 */
@Projection(name = "brief", types = {RoleAssignment.class}) 
public interface RoleAssignmentBrief extends AmpObjectBrief {

	public Long getUserId();
	public Long getRoleId();
	public Long getUnitId();
	
	@Value("#{target.user.username}")
	public String getUsername(); 

	@Value("#{target.role.name}")
	public String getRoleName(); 

	@Value("#{target.unit == null ? null : unit.name}")
	public String getUnitName(); 

}
