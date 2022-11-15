package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;

/**
 * Service for refreshing all access control related tables.
 * @author yingfeng
 */
public interface PermissionRefreshService {

	/**
	 * Refresh all Permission tables in the appropriate order.
	 * Note that refreshing a parent table might impact its child tables, which in turn might need a refresh. 
	 */
	public void refreshPermissionTables();
	
	public List<Role> refreshRole();
	
	public List<Action> refreshAction();
	
	public void refreshRoleAction(List<Role> roles, List<Action> actions);
	
}
