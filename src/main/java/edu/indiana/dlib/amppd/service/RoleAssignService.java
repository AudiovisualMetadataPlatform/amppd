package edu.indiana.dlib.amppd.service;

import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.model.projection.RoleBrief;
import edu.indiana.dlib.amppd.web.RoleAssignTable;
import edu.indiana.dlib.amppd.web.RoleAssignTuple;
import edu.indiana.dlib.amppd.web.RoleAssignUpdate;

/**
 * Service for role and role assignment related operations.
 * @author yingfeng
 */
public interface RoleAssignService {
	
	/**
	 * Check if the current user is AMP admin.
	 * @return true if the user is admin; false otherwise
	 */
	public boolean isAdmin();
	
	/**
	 * Assign AMP Admin role to the given user. 
	 */
	public RoleAssignment assignAdminRole(AmpUser user);
	
	/**
	 * Get the role assignment level threshold (excluding) for the current user in the given unit, based on the rules that
	 * a user can only assign roles weaker (with greater level value) than his strongest role (with least level value).  
	 * @param unitId ID of the given unit
	 * @return role assignment level threshold 
	 */
	public Integer getAssignableRoleLevel(Long unitId);
	
	/**
	 * Get all the roles the current user has permission to assign users to, within the given unit.
	 * @unitId ID of the unit associated with the role assignment
	 * @return the set of roles assignable by the current user
	 */
	public List<RoleBrief> getAssignableRoles(@RequestParam Long unitId);

	/**
	 * Get the users, roles, and assignment info for the current user and the given unit.
	 * @unitId ID of the unit associated with the role assignment
	 * @return the user-role assignment table 
	 */
	public RoleAssignTable retrieveRoleAssignments(Long unitId);

	/**
	 * Update the given role assignments within the given unit.
	 * @unitId ID of the given unit
	 * assignments list of user-role-assignment 
	 * @return the list of added/deleted RoleAssignments
	 */
	public RoleAssignUpdate updateRoleAssignments(Long unitId, List<RoleAssignTuple> assignments);

}
