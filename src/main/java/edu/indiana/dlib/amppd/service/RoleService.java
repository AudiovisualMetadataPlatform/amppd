package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.dto.RoleActionsDto;
import edu.indiana.dlib.amppd.model.dto.RoleActionsId;
import edu.indiana.dlib.amppd.web.RoleActionConfig;

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

	/**
	 * Retrieve global or unit-scope role_action configuration.
	 * @param unitId unit ID for unit-scope configuration, null if for global configuration
	 * @return instance of RoleActionConfig containing requested role_action permission info
	 */
	public RoleActionConfig retrieveRoleActionConfig(Long unitId);

	/**
	 * Update global or unit-scope role_action configuration with the given role_action configuration.
	 * @param unitId unit ID for unit-scope configuration, null if for global configuration
	 * @param roleActionsIds the given role_action configuration as a list of roles with IDs and a list or actions with IDs within each role 
	 * @return list of RoleActionsDtos successfully updated
	 */	
	public List<RoleActionsDto> updateRoleActionConfig(Long unitId, List<RoleActionsId> roleActionsIds);
	
}
