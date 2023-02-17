package edu.indiana.dlib.amppd.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
