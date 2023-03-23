package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.dto.RoleActionsDto;
import edu.indiana.dlib.amppd.model.dto.RoleActionsId;
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
	
	
	/**
	 * Retrieve global or unit-scope role_action configuration.
	 * @param unitId unit ID for unit-scope configuration, null if for global configuration
	 * @return instance of RoleActionConfig containing requested role_action permission info
	 */
	@GetMapping("/roleAssignments")
	public RoleActionConfig retrieveRoleActionConfig(Long unitId) {
		log.info("Retrieving role_action configuration within unit " + unitId);
		RoleActionConfig raConfig = roleService.retrieveRoleActionConfig(unitId);
		return raConfig;
	}

	/**
	 * Update global or unit-scope role_action configuration.
	 * @param unitId unit ID for unit-scope configuration, null if for global configuration
	 * @return list of RoleBriefActions successfully updated
	 */	
	public List<RoleActionsDto> updateRoleActionConfig(Long unitId, List<RoleActionsId> roleActions) {
		log.info("Updateing role_action configuration within unit " + unitId);
		List<RoleActionsDto> rolesUpdated = roleService.updateRoleActionConfig(unitId, roleActions);
		return rolesUpdated;		
	}


}
