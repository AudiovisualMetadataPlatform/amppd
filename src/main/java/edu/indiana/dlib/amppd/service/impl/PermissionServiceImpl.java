package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.projection.ActionBrief;
import edu.indiana.dlib.amppd.model.projection.RoleAssignmentDetail;
import edu.indiana.dlib.amppd.model.projection.RoleAssignmentDetailActions;
import edu.indiana.dlib.amppd.model.projection.RoleBrief;
import edu.indiana.dlib.amppd.model.projection.UnitBrief;
import edu.indiana.dlib.amppd.repository.ActionRepository;
import edu.indiana.dlib.amppd.repository.RoleAssignmentRepository;
import edu.indiana.dlib.amppd.repository.RoleRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.web.UnitActions;
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
	private RoleRepository roleRepository;

	@Autowired
	private ActionRepository actionRepository;

	@Autowired
	private RoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	private AmpUserService ampUserService;
	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.isAdmin()
	 */
	@Override
	public boolean isAdmin() {
		// find all role assignments for the current user
		AmpUser user = ampUserService.getCurrentUser();		

//		// get AMP Admin role
//		Role admin = roleRepository.findFirstByNameAndUnitIdIsNull(Role.AMP_ADMIN_ROLE_NAME);
//		
//		// check whether the user has a role assignment with AMP Admin
//		boolean is = roleAssignmentRepository.existsByUserIdAndRoleIdAndUnitIdIsNull(user.getId(), admin.getId());
		boolean is = roleAssignmentRepository.existsByUserIdAndRoleNameAndUnitIdIsNull(user.getId(), Role.AMP_ADMIN_ROLE_NAME);		
		String isstr = is ? "is" : "is not";
		
		log.info("The current user " + isstr + " " + Role.AMP_ADMIN_ROLE_NAME);
		return is;
	}				
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAccessibleUnits()
	 */
	@Override
	public Set<UnitBrief> getAccessibleUnits() {
		Set<UnitBrief> units = new HashSet<UnitBrief>();

		// if current user is AMP Admin, then all units are accessible
		AmpUser user = ampUserService.getCurrentUser();		
		if (isAdmin()) {
			units = unitRepository.findAllProjectedBy();			
			log.info("The current user " + user.getUsername() + " is Admin and has access to all " + units.size() + " units." );
			return units;
		}
		
		// find all role assignments for current user
		List<RoleAssignmentDetail> ras = roleAssignmentRepository.findByUserIdAndUnitIdNotNull(user.getId());
		
		// retrieve the associated units
		for (RoleAssignmentDetail ra : ras) {
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
	public Set<Long> getAccessibleUnits(ActionType actionType, TargetType targetType) {
		Set<Long> unitIds = new HashSet<Long>();

		// if current user is AMP Admin, then all units are accessible, return null to indicate no restraints on unit ID
		AmpUser user = ampUserService.getCurrentUser();		
		if (isAdmin()) {
			log.info("The current user " + user.getUsername() + " is admin and can perform action <" + actionType + ", " + targetType + "> in all units." );
			return null;
		}
		
		// find the action
		Action action = actionRepository.findFirstByActionTypeAndTargetType(actionType, targetType);
		if (action == null) {
			throw new StorageException("Failed to find action for: " + "actionType = " + actionType + ", targetType = " + targetType);
		}
		
		// find all role assignments for current user
		List<RoleAssignmentDetail> ras = roleAssignmentRepository.findByUserIdAndUnitIdNotNull(user.getId());
		
		// retrieve the associated units if the role can perform the action
		Set<Role> roles = action.getRoles();
		for (RoleAssignmentDetail ra : ras) {
			RoleBrief role = ra.getRole();
			if (roles.contains(role)) {
				Long unitId = ra.getUnit().getId();
				unitIds.add(unitId);
			}
		}
		
		// throw access denied exception if the user can't perform the action in any unit at all
		if (unitIds.isEmpty()) {
			throw new AccessDeniedException("The current user " + user.getUsername() + " cannot perform action " + actionType + " " + targetType  + " in any unit.");
		}
		
		log.info("The current user " + user.getUsername() + " can perform action " + actionType + " " + targetType + " in " + unitIds.size() + " units." );
		return unitIds;
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
	 * @see edu.indiana.dlib.amppd.service.PermissionService.prefilter(WorkflowResultSearchQuery)
	 */
	@Override
	public boolean prefilter(WorkflowResultSearchQuery query) {
		Set<Long> accessibleUnits = getAccessibleUnits(ActionType.Read, TargetType.WorkflowResult);
		Long[] fus = query.getFilterByUnits();
		Set<Long> filterUnits = Set.of(fus);
		boolean filtered = false;
		
		// for admin, i.e. accessibleUnits is null, no AC prefilter needed;  
		// otherwise apply AC prefilter to user filters
		if (accessibleUnits != null) {
			// if no user filter defined, use the AC filter
			if (filterUnits.isEmpty()) {
				filterUnits = accessibleUnits;
			}
			// otherwise retain only permitted units in user filters
			else {
				filterUnits.retainAll(accessibleUnits);
				// if after applying AC filter the query filters becomes empty, then user cannot perform this query
				if (filterUnits.isEmpty()) {
					throw new AccessDeniedException("The current user cannot query workflow results in the filtered units.");
				}
			}

			// update unit filters in query
			filtered = true;
			fus = filterUnits.toArray(fus);
			query.setFilterByUnits(fus);
		}
		
		return filtered;
	}
	
}
