package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.ac.Role;


/**
 * Projection for a detailed view of a role.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Role.class}) 
public interface RoleDetail extends RoleBriefActions, AmpObjectDetail {

	public UnitBrief getUnit();
    public Set<RoleAssignmentBrief> getRoleAssignements();	

}