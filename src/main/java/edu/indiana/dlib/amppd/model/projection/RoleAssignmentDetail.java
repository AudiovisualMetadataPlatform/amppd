package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.ac.RoleAssignment;


/**
 * Projection for a detailed view of a role.
 * @author yingfeng
 */
@Projection(name = "detail", types = {RoleAssignment.class}) 
public interface RoleAssignmentDetail extends RoleAssignmentBrief, AmpObjectDetail {

	public AmpUserBrief getUser();
    public RoleBrief getRole();	
	public UnitBrief getUnit();

}