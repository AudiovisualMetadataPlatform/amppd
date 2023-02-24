package edu.indiana.dlib.amppd.service;

import java.util.List;

import org.springframework.http.HttpMethod;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;

/**
 * Service for access control permission checking related operations.
 * @author yingfeng
 */
public interface PermissionService {

	/**
	 * Get the list of units in which the current user has at least some access to, i.e. has some role assignments.
	 * @return the list of units the current user has access to
	 */
	public List<Unit> getAccessibleUnits();
	
	/**
	 * Check if the current user has permission to perform the given action on the given target in the given unit.
	 * @param actionType type of the given action
	 * @param targetType type of the given target
	 * @param unitId ID of the given unit
	 * @return true if the user has the permission; false otherwise
	 */
	public boolean hasPermsion(ActionType actionType, TargetType targetType, Long unitId);
	
	/**
	 * Check if the current user has permission to issue the given request with the given URL in the given unit.
	 * @param httpMethod HTTP method of the given request
	 * @param urlPattern URL pattern of the given request
	 * @param unitId ID of the given unit
	 * @return true if the user has the permission; false otherwise
	 */
	public boolean hasPermsion(HttpMethod httpMethod, String urlPattern, Long unitId);
	
	/**
	 * Check if the current user has permission to perform the given action in the given unit.
	 * @param action the given action
	 * @param unitId ID of the given unit
	 * @return true if the user has the permission; false otherwise
	 */
	public boolean hasPermission(Action action, Long unitId);
	
}
