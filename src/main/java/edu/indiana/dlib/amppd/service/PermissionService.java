package edu.indiana.dlib.amppd.service;

import org.springframework.http.HttpMethod;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;

/**
 * Service for access control permission checking related operations.
 * @author yingfeng
 */
public interface PermissionService {

	public boolean hasPermsion(ActionType actionType, TargetType targetType, Long unitId);
	
	public boolean hasPermsion(HttpMethod httpMethod, String urlPattern, Long unitId);
	
	public boolean hasPermission(Action action, Long unitId);
	
}
