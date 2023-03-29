package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
	@GetMapping("/roles/config")
	public RoleActionConfig retrieveRoleActionConfig(@RequestParam(required = false) Long unitId) {
		log.info("Retrieving role_action permission configuration within unit " + unitId);
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
		log.info("Updateing role_action permission configuration within unit " + unitId);
		List<RoleActionsDto> rolesUpdated = roleService.updateRoleActionConfig(unitId, roleActionsIds);
		return rolesUpdated;		
	}


}
