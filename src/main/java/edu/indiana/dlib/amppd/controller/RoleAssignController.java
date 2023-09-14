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
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.service.RoleAssignService;
import edu.indiana.dlib.amppd.web.RoleAssignTable;
import edu.indiana.dlib.amppd.web.RoleAssignTuple;
import edu.indiana.dlib.amppd.web.RoleAssignUpdate;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller to handle requests for role assignment.
 * @author yingfeng
 */
@RestController
@Slf4j
public class RoleAssignController {

	@Autowired
	private RoleAssignService roleAssignService;
	
	@Autowired
	private PermissionService permissionService;

	
	/**
	 * Check if the current user is AMP admin.
	 * @return true if the user is admin; false otherwise
	 */
	@GetMapping("/roleAssignments/isAdmin")
	public boolean isAdmin() {
		log.info("Checking if the current user is AMP admin ...");
		return roleAssignService.isAdmin();		
	}
		
	/**
	 * Get the users, roles, and assignment info for the current user and the given unit.
	 * @unitId ID of the unit associated with the role assignment
	 * @return the user-role assignment table 
	 */
	@GetMapping("/roleAssignments")
	public RoleAssignTable retrieveRoleAssignments(@RequestParam Long unitId) {
		// check permission 
		Long acUnitId = unitId;
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.RoleAssignment, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot view role assignment in unit " + acUnitId);
		}
				
		log.info("Retrieving user-role assignments within unit " + unitId);
		RoleAssignTable rat = roleAssignService.retrieveRoleAssignments(unitId);
		return rat;
	}

	/**
	 * Update the given role assignments within the given unit.
	 * @unitId ID of the given unit
	 * assignments list of user-role-assignment 
	 * @return the pair of Lists of the added/deleted roleAssignments
	 */
	@PostMapping("/roleAssignments")
	public RoleAssignUpdate updateRoleAssignments(@RequestParam Long unitId, @RequestBody List<RoleAssignTuple> assignments) {
		// check permission 
		// note: the checking on RoleAssignment level for CollectionManager/UnitManager is done 
		// within updateRoleAssignments as part of RoleAssignment validation instead of AC
		Long acUnitId = unitId;
		boolean can = permissionService.hasPermission(ActionType.Update, TargetType.RoleAssignment, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update role assignment in unit " + acUnitId);
		}
		
		log.info("Updating + " + assignments.size() + " role assignments within unit " + unitId);
		RoleAssignUpdate rau = roleAssignService.updateRoleAssignments(unitId, assignments);
		return rau;
	}
		

}
