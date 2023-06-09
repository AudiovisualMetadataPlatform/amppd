package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.projection.ActionBrief;
import edu.indiana.dlib.amppd.model.projection.RoleAssignmentDetailActions;
import edu.indiana.dlib.amppd.model.projection.UnitBrief;
import edu.indiana.dlib.amppd.repository.ActionRepository;
import edu.indiana.dlib.amppd.repository.RoleAssignmentRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.service.RoleAssignService;
import edu.indiana.dlib.amppd.web.UnitActions;
import edu.indiana.dlib.amppd.web.WorkflowResultFilterUnit;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementation of PermissionService.
 * @author yingfeng
 */
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {	
	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private ActionRepository actionRepository;

	@Autowired
	private RoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	private AmpUserService ampUserService;
		
	@Autowired
	private RoleAssignService roleAssignService;


	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.hasPermsion(ActionType, TargetType, Long)
	 */
	@Override
	public boolean hasPermsion(ActionType actionType, TargetType targetType, Long unitId) {
		Action action = actionRepository.findFirstByActionTypeAndTargetType(actionType, targetType);		
		boolean has = hasPermission(action, unitId);
		return has;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.hasPermsion(HttpMethod, String, Long)
	 */
	@Override
	public boolean hasPermsion(HttpMethod httpMethod, String urlPattern, Long unitId) {
		Action action = actionRepository.findFirstByHttpMethodAndUrlPattern(httpMethod, urlPattern);		
		boolean has = hasPermission(action, unitId);
		return has;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.hasPermission(Action, Long)
	 */
	@Override
	public boolean hasPermission(Action action, Long unitId) {		
		// find the current user
		AmpUser user = ampUserService.getCurrentUser();		
		
		// find all roles that can perform the action
		Set<Role> roles = action.getRoles();		
		List<Long> roleIds = new ArrayList<Long>();		
		for (Role role : roles) {
			roleIds.add(role.getId());
		}
		
		// check if the current user is assigned to one of the above roles
		// the only case when role assignment unit is null is for AMP Admin, who has permission for all actions;
		// otherwise the role assignment must be associated with some unit
		boolean has = roleAssignmentRepository.existsByUserIdAndRoleIdInAndUnitIdIsNull(user.getId(), roleIds);
		has = has || roleAssignmentRepository.existsByUserIdAndRoleIdInAndUnitId(user.getId(), roleIds, unitId);
		
		String hasstr = has ? "has" : "has no";
		log.info("Current user " + user.getUsername() + " " + hasstr + " permission to perform action " + action.getName() +" in unit " + unitId);				
		return has;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getPermittedActions(List<ActionType>, List<TargetType>, List<Long>)
	 */
	@Override
	public List<UnitActions> getPermittedActions(List<ActionType> actionTypes, List<TargetType> targetTypes, List<Long> unitIds) {
		List<UnitActions> uas = new ArrayList<UnitActions>();
		
		// find all role assignments for the current user per given units ordered by unit ID
		AmpUser user = ampUserService.getCurrentUser();		
		List<RoleAssignmentDetailActions> ras = unitIds.isEmpty() ?
			roleAssignmentRepository.findByUserIdOrderByUnitId(user.getId()) : 
			roleAssignmentRepository.findByUserIdAndUnitIdInOrderByUnitId(user.getId(), unitIds);
		
		// for all the user's roles within each unit, merge the unique actions matching the actionTypes and/or targetTypes
		UnitActions ua = new UnitActions(0L, new HashSet<ActionBrief>());	// current UnitActions
		for (RoleAssignmentDetailActions ra : ras) {
			Long unitId = ra.getUnitId();		
			
			// skip global role assignment, where unit is null
			if (unitId == null) {
				continue;
			}
			
			// when the current role assignment's unit ID is a new one
			if (!unitId.equals(ua.getUnitId())) {
				// if the previous UnitActions contains any actions, add it to the parent list
				if (!ua.getActions().isEmpty()) {
					uas.add(ua);
				}

				// start a new UnitAction as the current one
				ua = new UnitActions(unitId, new HashSet<ActionBrief>());	
			}
			
			// merge the actions for the current role into current UnitActions  
			Set<ActionBrief> actionsU = ua.getActions();
			Set<ActionBrief> actionsR = ra.getRole().getActions();
			for (ActionBrief action : actionsR) {
				// the current action must be unique, i.e. not already added to the UnitAction list yet
				boolean match = !actionsU.contains(action);
				
				// if actionTypes is specified, the current actionType must be one of them
				if (!actionTypes.isEmpty()) {
					match = match && actionTypes.contains(action.getActionType());
				}

				// if targetTypes is specified, the current targetType must be one of them
				if (!targetTypes.isEmpty()) {
					match = match && targetTypes.contains(action.getTargetType());
				}
				
				// add the action if all criteria satisfy
				if (match) {
					actionsU.add(action);
				}
			}			
		}
			
		// add the last UnitActions to the parent list if containing any actions
		if (!ua.getActions().isEmpty()) {
			uas.add(ua);
		}		
		
		log.info("Successfully found all permitted actions in " + uas.size() + " units for the current user " + user.getUsername());
		return uas;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAccessibleUnitIds()
	 */
	@Override
	public Set<UnitBrief> getAccessibleUnits() {
		Set<UnitBrief> units = new HashSet<UnitBrief>();

		// if current user is AMP Admin, then all units are accessible
		AmpUser user = ampUserService.getCurrentUser();		
		if (roleAssignService.isAdmin()) {
			units = unitRepository.findAllProjectedBy();			
			log.info("The current user " + user.getUsername() + " is Admin and has access to all " + units.size() + " units." );
			return units;
		}
		
		// find all role assignments for current user
		List<RoleAssignmentDetailActions> ras = roleAssignmentRepository.findByUserIdAndUnitIdNotNull(user.getId());
		
		// retrieve the associated units
		for (RoleAssignmentDetailActions ra : ras) {
			UnitBrief unit = ra.getUnit();
			units.add(unit);
		}
		
		log.info("The current user " + user.getUsername() + " has access to " + units.size() + " units." );
		return units;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAccessibleUnits(ActionType, TargetType)
	 */
	@Override
	public Set<UnitBrief> getAccessibleUnits(ActionType actionType, TargetType targetType) {
		Set<UnitBrief> units = new HashSet<UnitBrief>();

		// return null to signal that the current user is AMP Admin, in which case all units are accessible
		AmpUser user = ampUserService.getCurrentUser();		
		if (roleAssignService.isAdmin()) return null;
			
		// find all role assignments for current user
		List<RoleAssignmentDetailActions> ras = roleAssignmentRepository.findByUserIdAndUnitIdNotNull(user.getId());
		
		// retrieve the associated units if the role can perform the action
		for (RoleAssignmentDetailActions ra : ras) {
			UnitBrief unit = ra.getUnit();
		
			// skip if the unit of this assignment is already included
			if (units.contains(unit)) continue;
			
			// include the unit of this assignment if the role can perform the action
			for (ActionBrief action : ra.getRole().getActions()) {
				if (action.getActionType() == actionType && action.getTargetType() == targetType) {
					units.add(unit);
					break;
				}
			}
		}
		
		return units;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAccessibleUnitIds(ActionType, TargetType)
	 */
	@Override
	public Set<Long> getAccessibleUnitIds(ActionType actionType, TargetType targetType) {
		Set<Long> unitIds = new HashSet<Long>();
		
		// get accessible units for the action
		Set<UnitBrief> units = getAccessibleUnits(actionType, targetType);		
		
		// if units is null, i.e. current user is AMP Admin, return null as well to indicate no restraints on unit ID
		if (units == null ) {
			log.info("The current user is admin and can perform action " + actionType + " " + targetType + " in any unit." );
			return null;
		}
			
		// otherwise, if units is empty, throw access denied exception as the user can't perform the action in any unit
		if (units.isEmpty()) {
			throw new AccessDeniedException("The current user cannot perform action " + actionType + " " + targetType  + " in any unit.");
		}
		
		// otherwise retrieve the accessible units IDs
		for (UnitBrief unit : units) {
			unitIds.add(unit.getId());
		}
		
		log.info("The current user can perform action " + actionType + " " + targetType + " in " + units.size() + " units." );
		return unitIds;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.prefilter(WorkflowResultSearchQuery)
	 */
	@Override
	public Set<Long> prefilter(WorkflowResultSearchQuery query) {
		// get accessible units for Read WorkflowResult, if none, access denied exception will be thrown
		Set<Long> acUnitIds = getAccessibleUnitIds(ActionType.Read, TargetType.WorkflowResult);

		// otherwise if acUnitIds is null, i.e. user is admin, then no AC prefilter is needed;  
		if (acUnitIds == null) return acUnitIds;
		
		// otherwise apply AC prefilter by intersecting with user filters		
		Long[] fus = query.getFilterByUnits();
		Set<Long> ftUnitIds = Set.of(fus);
		
		// retain user filtered units if exist, among all accessible units
		if (!ftUnitIds.isEmpty()) {
			// note: ftUnitIds.retainAll(acUnitIds) won't work as ftUnitIds is immutable
			acUnitIds.retainAll(ftUnitIds);
		}

		// if above intersection is empty, the user cannot perform this query
		if (acUnitIds.isEmpty()) {
			throw new AccessDeniedException("The current user cannot query workflow results within the filtered units.");
		}

		// update unit filters in query if changed
		Long[] fusNew = acUnitIds.toArray(fus);	
		
		// note: fusNew.length < fus.length won't work in case fus is empty
		if (fusNew.length != fus.length) {
			query.setFilterByUnits(fusNew);
			log.info("WorkflowResultSearchQuery has been prefiltered with only accessible units.");
		}
		else {
			log.info("WorkflowResultSearchQuery reamins the same after AC units prefilter.");				
		}			
		
		return acUnitIds;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.postfilter(WorkflowResultResponse, Set<Long>)
	 */
	@Override
	public void postfilter(WorkflowResultResponse response, Set<Long> acUnitIds) {
		List<WorkflowResultFilterUnit> fus = response.getFilters().getUnits();
		List<WorkflowResultFilterUnit> fusNew = new ArrayList<WorkflowResultFilterUnit>();

		// if acUnitIds is null, i.e. user is admin, then no AC prefilter is needed;  
		if (acUnitIds == null) return;

		// otherwise apply AC postfilter by intersecting with user filters
		for (WorkflowResultFilterUnit fu : fus) {
			if (acUnitIds.contains(fu.getUnitId())) {
				fusNew.add(fu);
			}
		}

		// update unit filters in response if changed
		if (fusNew.size() < fus.size()) {
			response.getFilters().setUnits(fusNew);
			log.info("WorkflowResultResponse has been postfiltered with only accessible units.");
		}
		else {
			log.info("WorkflowResultResponse reamins the same after AC units postfilter.");				
		}			
	}
	
	
}
