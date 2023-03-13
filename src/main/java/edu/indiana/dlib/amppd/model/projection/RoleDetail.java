package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;


/**
 * Projection for a detailed view of a role.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Role.class}) 
public interface RoleDetail extends RoleBrief, AmpObjectDetail {

	public Unit getUnit();
	public Set<Action> getActions();
    public Set<RoleAssignment> getRoleAssignements();	

}