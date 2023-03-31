package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.ac.RoleAssignment;


/**
 * Projection for a detailed view of a role.
 * @author yingfeng
 */
@Projection(name = "detailActions", types = {RoleAssignment.class}) 
public interface RoleAssignmentDetailActions extends RoleAssignmentBrief, AmpObjectDetail {

	public AmpUserBrief getUser();
    public RoleBriefActions getRole();	
	public UnitBrief getUnit();

}