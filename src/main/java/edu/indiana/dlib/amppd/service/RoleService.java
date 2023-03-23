package edu.indiana.dlib.amppd.service;

import java.util.List;

/**
 * Service for role and role related operations.
 * @author yingfeng
 */
public interface RoleService {
	
	/**
	 * Get the list of unit-scope role names.
	 * @return list of unit-scope role names.
	 */
	public List<String> getUnitRoleNames();

}
