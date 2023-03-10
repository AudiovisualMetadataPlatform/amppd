package edu.indiana.dlib.amppd.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.ac.Role;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller to handle requests for access control.
 * @author yingfeng
 */
@RestController
@Slf4j
public class RoleController {

	/**
	 * Get all the roles the current user has permission to assign users to, within the given unit.
	 * @unitId ID of the unit associated with the role assignment
	 */
	public List<Role> getAssignableRoles(@RequestParam Long unitId) {
		List<Role> roles = new ArrayList<Role>();
		
		return roles;
	}
	
}
