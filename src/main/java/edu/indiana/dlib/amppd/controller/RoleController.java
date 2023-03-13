package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.service.RoleService;
import edu.indiana.dlib.amppd.web.RoleAssignRequest;
import edu.indiana.dlib.amppd.web.RoleAssignResponse;
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
	@GetMapping("/roleAssignments")
	public RoleAssignResponse retrieveRoleAssignments(@RequestParam Long unitId) {
		log.info("Retrieving user-role assignments for unit " + unitId);
		RoleAssignResponse response = roleService.retrieveRoleAssignments(unitId);
		return response;
	}

	/**
	 * Update the given role assignments within the given unit.
	 * @unitId ID of the given unit
	 * assignments list of user-role-assignment 
	 * @return the updated roleAssignments
	 */
	@PostMapping("/roleAssignments")
	public List<RoleAssignment> updateRoleAssignments(@RequestParam Long unitId, @RequestBody List<RoleAssignRequest> assignments) {
		log.info("Updating + " + assignments.size() + " within unit " + unitId);
		List<RoleAssignment> ras = roleService.updateRoleAssignments(unitId, assignments);
		return ras;
	}
		

}
