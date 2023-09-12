package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.dto.RoleActionsDto;
import edu.indiana.dlib.amppd.model.dto.RoleActionsId;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.service.RoleService;
import edu.indiana.dlib.amppd.web.RoleActionConfig;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller to handle requests for role related operations.
 * @author yingfeng
 */
@RestController
@Slf4j
public class RoleController {
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private PermissionService permissionService;

	
	/**
	 * Retrieve global or unit-scope role_action configuration.
	 * @param unitId unit ID for unit-scope configuration, null if for global configuration
	 * @return instance of RoleActionConfig containing requested role_action permission info
	 */
	@GetMapping("/roles/config")
	public RoleActionConfig retrieveRoleActionConfig(@RequestParam(required = false) Long unitId) {
		// check permission 
		Long acUnitId = unitId;
		String rolestr = acUnitId == null ? "of global roles" : "of roles in unit " + acUnitId;
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Role, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot view permission configuration " + rolestr);
		}
		
		log.info("Retrieving role_action permission configuration " + rolestr);
		RoleActionConfig raConfig = roleService.retrieveRoleActionConfig(unitId);
		return raConfig;
	}

	/**
	 * Update global or unit-scope role_action configuration with the given role_action configuration.
	 * @param unitId unit ID for unit-scope configuration, null if for global configuration
	 * @param roleActionsIds the given role_action configuration as a list of roles with IDs and a list or actions with IDs within each role 
	 * @return list of RoleActionsDtos successfully updated
	 */	
	@PostMapping("/roles/config")
	public List<RoleActionsDto> updateRoleActionConfig(@RequestParam(required = false) Long unitId, @RequestBody List<RoleActionsId> roleActionsIds) {
		// check permission 
		Long acUnitId = unitId;
		String rolestr = acUnitId == null ? "of global roles" : "of roles in unit " + acUnitId;
		boolean can = acUnitId == null ? 
			permissionService.hasPermission(ActionType.Update, TargetType.Role, null) :
			permissionService.hasPermission(ActionType.Update, TargetType.Role_Unit, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update permission configuration " + rolestr);
		}
		
		log.info("Updating role_action permission configuration " + rolestr);
		List<RoleActionsDto> rolesUpdated = roleService.updateRoleActionConfig(unitId, roleActionsIds);
		return rolesUpdated;		
	}


}
