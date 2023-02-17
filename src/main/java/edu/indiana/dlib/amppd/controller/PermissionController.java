package edu.indiana.dlib.amppd.controller;



import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.service.PermissionService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to handle requests for access control.
 * @author yingfeng
 */
@RestController
@Slf4j
public class PermissionController {

	@Autowired
    private PermissionService permissionService;
	
	
	/**
	 * Get the list of units in which the current user has at least some access to, i.e. has some role assignments assoicated with.
	 * @return the list of units the current user has access to
	 */
	@GetMapping("/permissions/accessibleUnits")
	public List<Unit> getAccessibleUnits() {
		log.info("Retrieving all units the current user has access to ...");
		return permissionService.getAccessibleUnits();
	}
	
	/**
	 * Check if the current user has permission to perform the given action or issue the given request in the given unit.
	 * @param actionType type of the given action
	 * @param targetType type of the given target
	 * @param httpMethod HTTP method of the given request
	 * @param urlPattern URL pattern of the given request
	 * @param unitId ID of the given unit
	 * @return true if the user has the permission; false otherwise
	 */
	@GetMapping("/permissions/has")
	public boolean hasPermission(
			@RequestParam(required = false) ActionType actionType, 
			@RequestParam(required = false) TargetType targetType, 
			@RequestParam(required = false) HttpMethod httpMethod, 
			@RequestParam(required = false) String urlPattern, 
			@RequestParam(required = false) Long unitId) {
		boolean has = false;
		
		if (actionType != null && targetType != null) {
			has = permissionService.hasPermsion(actionType, targetType, unitId);
			log.info("Checking current user permission to perform action " + actionType + " on target " + targetType + " in unit " + unitId);
		}
		else if (httpMethod != null && urlPattern != null) {
			has = permissionService.hasPermsion(httpMethod, urlPattern, unitId);
			log.info("Checking current user permission to send request " + httpMethod + " " + urlPattern + " in unit " +  + unitId);
		} 
		
		return has;
	}
	
}
