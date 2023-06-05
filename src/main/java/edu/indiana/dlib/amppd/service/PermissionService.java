package edu.indiana.dlib.amppd.service;

import java.util.List;
import java.util.Set;

import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.projection.UnitBrief;
import edu.indiana.dlib.amppd.web.UnitActions;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;

/**
 * Service for access control permission checking related operations.
 * @author yingfeng
 */
public interface PermissionService {

	/**
	 * Check if the current user is AMP admin.
	 * @return true if the user is admin; false otherwise
	 */
	public boolean isAdmin();

	/**
	 * Get the units in which the current user has at least some access to, i.e. has some role assignments.
	 * @return the list of units the current user has access to
	 */
	public Set<UnitBrief> getAccessibleUnits();
	
	/**
	 * Get the units in which the current user can perform the given action. 
	 * @param actionType actionType of the given action
	 * @param targetType targetType of the given action
	 * @return null if the user is admin; empty set if the user can't perform the action in any unit; or the set of IDs for such units.
	 */
	public Set<Long> getAccessibleUnits(ActionType actionType, TargetType targetType);
	
	/**
	 * Get the actions the current user can perform, given the list of actionTypes, targetTypes and units;
	 * if actionTypes not provided, get for all actionTypes;
	 * if targetTypes not provided, get for all targetTypes;
	 * if units not provided, get for all units.
	 * Note: Actions for global role assignments are excluded from the returned list, as actions for such roles are cross units; 
	 * Currently, the only global role is AMP admin, who can perform all actions across all units. 
	 * which can perform all actions in all units.
	 * @param actionTypes types of the queried actions
	 * @param targetTypes targets of the queried actions
	 * @param unitIds IDs of the units the action target belongs to
	 * @return list of permitted actions per unit
	 */
	public List<UnitActions> getPermittedActions(List<ActionType> actionTypes, List<TargetType> targetTypes, List<Long> unitIds);
	
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
	
	/**
	 * Apply access control prefilter to the given WorkflowResultSearchQuery.
	 * @param query the given WorkflowResultSearchQuery
	 * @return the set of accessible units for the current user on WorkflowResult Search
	 * @throws AccessDeniedException if the current user is not allowed to view WorkflowResult in any unit or for the user defined unit filters
	 */
	public Set<Long> prefilter(WorkflowResultSearchQuery query);
	
	/**
	 * Apply access control postfilter to the given WorkflowResultResponse with the given accessibleUnits
	 * @param response the given WorkflowResultResponse
	 * @param accessibleUnits the set of given accessibleUnits
	 */
	public void postfilter(WorkflowResultResponse response, Set<Long> accessibleUnits);
	

}
