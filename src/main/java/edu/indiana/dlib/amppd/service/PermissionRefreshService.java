package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.dto.RoleAction;

/**
 * Service for refreshing all access control related tables.
 * @author yingfeng
 */
public interface PermissionRefreshService {

	/**
	 * Refresh all static data for roles, actions, and permissions in the appropriate order from their corresponding predefined csv files.
	 * Note that refreshing a parent table might impact its child tables, which in turn might need a refresh. 
	 */
	public void refreshPermissionTables();
	
	/**
	 * Refresh roles from its corresponding csv file.
	 * @return list of updated roles
	 */
	public List<Role> refreshRole();
	
	/**
	 * Refresh actions from its corresponding csv file.
	 * @return list of updated actions
	 */
	public List<Action> refreshAction();
	
	/**
	 * Refresh permissions from its corresponding csv file, given the current role hierarchy depth.
	 * @param roleDepth the depth of the role hierarchy, which equals the total number of roles
	 * @return the list of RoleActions parsed from csv.
	 */
	public List<RoleAction> refreshRoleAction(int roleDepth);
	
}
