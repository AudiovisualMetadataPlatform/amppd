package edu.indiana.dlib.amppd.service;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.projection.UnitBrief;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.UnitActions;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;

/**
 * Service for access control permission checking related operations.
 * @author yingfeng
 */
public interface PermissionService {

	/**
	 * Check if the current user can Read the dataentity with the given ID and type.
	 * @param id ID of the dataentity
	 * @param clazz class of the dataentity
	 * @return true if the user has the permission; false otherwise
	 */
	public boolean hasReadPermission(Long id, Class clazz);
	
	/**
	 * Check if the current user has permission to perform the given action on the given target in the given unit 
	 * or at least one of the units.
	 * @param actionType type of the given action
	 * @param targetType type of the given target
	 * @param unitId ID of the given unit; null means at least one of the units
	 * @return true if the user has the permission; false otherwise
	 */
	public boolean hasPermission(ActionType actionType, TargetType targetType, Long unitId);
	
	/**
	 * Check if the current user has permission to issue the given request with the given URL in the given unit 
	 * or at least one of the units.
	 * @param httpMethod HTTP method of the given request
	 * @param urlPattern URL pattern of the given request
	 * @param unitId ID of the given unit; null means at least one of the units
	 * @return true if the user has the permission; false otherwise
	 */
	public boolean hasPermission(HttpMethod httpMethod, String urlPattern, Long unitId);
	
	/**
	 * Check if the current user has permission to perform the given action in the given unit 
	 * or at least one of the units.
	 * @param action the given action
	 * @param unitId ID of the given unit; null means at least one of the units
	 * @return true if the user has the permission; false otherwise
	 */
	public boolean hasPermission(Action action, Long unitId);
	
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
	 * Get the units in which the current user has at least some access to, i.e. has some role assignments.
	 * @return the list of units the current user has access to
	 */
	public Set<UnitBrief> getAccessibleUnits();
	
	/**
	 * Get the units in which the current user can perform the given action. 
	 * @param actionType actionType of the given action
	 * @param targetType targetType of the given action
	 * @return the set of accessible units (empty if the user can't perform the action in any unit), or null if the user is admin; 
	 */
	public ImmutablePair<Set<Long>, Set<UnitBrief>> getAccessibleUnits(ActionType actionType, TargetType targetType);

	/**
	 * Get the IDs of the units in which the current user can perform the given action. 
	 * @param actionType actionType of the given action
	 * @param targetType targetType of the given action
	 * @return the set of accessible units IDs (empty if the user can't perform the action in any unit), or null if the user is admin; 
	 */
	public Set<Long> getAccessibleUnitIds(ActionType actionType, TargetType targetType);
	
	/**
	 * Get the unit ID the given object belongs to for the purpose of access control.
	 * @param id ID of the given object
	 * @param clazz class of the given object, must be WorkflowResult or a subclass of Dataentity
	 * @return the access control unit ID of the object
	 */
	public Long getAcUnitId(Long id, Class clazz);
	
	/**
	 * Apply access control prefilter to the given WorkflowResultSearchQuery for the given action.
	 * If action is not specified, it defaults to Read WorkflowResult.
	 * @param query the given WorkflowResultSearchQuery
	 * @param actionType actionType of the given action
	 * @param targetType targetType of the given action
	 * @return the set of IDs of accessible units for the current user on WorkflowResult Search
	 * @throws AccessDeniedException if the current user is not allowed to view WorkflowResult in any unit or for the user defined unit filters
	 */
	public Set<Long> prefilter(WorkflowResultSearchQuery query, ActionType actionType, TargetType targetType);
	
	/**
	 * Apply access control postfilter to the given WorkflowResultResponse with the given accessible units.
	 * @param response the given WorkflowResultResponse
	 * @param acUnitIds the set of IDs of the given accessible units
	 */
	public void postfilter(WorkflowResultResponse response, Set<Long> acUnitIds);
	
	/**
	 * Apply access control prefilter to the given MgmEvaluationSearchQuery.
	 * @param query the given MgmEvaluationSearchQuery
	 * @return the set of IDs of accessible units for the current user on MgmEvaluationTest Search
	 * @throws AccessDeniedException if the current user is not allowed to view MgmEvaluationTest in any unit or for the user defined unit filters
	 */
	public Set<Long> prefilter(MgmEvaluationSearchQuery query);
	
	/**
	 * Apply access control postfilter to the given MgmEvaluationTestResponse with the given accessible units.
	 * @param response the given MgmEvaluationTestResponse
	 * @param acUnitIds the set of IDs of the given accessible units
	 */
	public void postfilter(MgmEvaluationTestResponse response, Set<Long> acUnitIds);
	
}
