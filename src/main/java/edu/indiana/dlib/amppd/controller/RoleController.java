package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.RoleService;
import edu.indiana.dlib.amppd.web.RoleAssignTable;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller to handle requests for access control.
 * @author yingfeng
 */
@RestController
@Slf4j
public class RoleController {

	@Autowired
	private RoleService roleService;
	
	
	/**
	 * Get the users, roles, and assignment info for the current user and the given unit.
	 * @unitId ID of the unit associated with the role assignment
	 * @return the user-role assignment table 
	 */
	@GetMapping("/roleAssignments/table")
	public RoleAssignTable getUserRoleAssignments(@RequestParam Long unitId) {
		log.info("Retrieving user-role assignments for unit " + unitId);
		RoleAssignTable ratable = roleService.getUserRoleAssignments(unitId);
		return ratable;
	}
		
}
