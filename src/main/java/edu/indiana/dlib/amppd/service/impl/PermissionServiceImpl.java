package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.IterableUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.repository.ActionRepository;
import edu.indiana.dlib.amppd.repository.RoleAssignmentRepository;
import edu.indiana.dlib.amppd.repository.RoleRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.web.UnitActions;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementation of PermissionService.
 * @author yingfeng
 */
@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {	
	public static String AMP_ADMIN_ROLE_NAME = "AMP Admin";

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
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAccessibleUnits()
	 */
	@Override
	public Set<Unit> getAccessibleUnits() {
		Set<Unit> units = new HashSet<Unit>();

		// find all role assignments for the current user
		AmpUser user = ampUserService.getCurrentUser();		
		List<RoleAssignment> ras = roleAssignmentRepository.findByUserId(user.getId());
		
		// retrieve the associated units
		for (RoleAssignment ra : ras) {
			Unit unit = ra.getUnit();
			
			// if an assignment is not associated with any unit, it's a global one, which means the user has AMP Admin role and can access all units 
			if (unit == null) {
				Iterable<Unit> allunits = unitRepository.findAll();
				units.addAll(IterableUtils.toList(allunits));
				log.info("The current user " + user.getUsername() + " is AMP Admin and thus has access to all " + units.size() + " units." );
				return units;
			}
			
			// otherwise add the associated unit to the list
			units.add(unit);
		}
		
		log.info("The current user " + user.getUsername() + " has access to " + units.size() + " units." );
		return units;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.isAdmin()
	 */
	@Override
	public boolean isAdmin() {
		// find all role assignments for the current user
		AmpUser user = ampUserService.getCurrentUser();		

		// get AMP Admin role
		Role admin = roleRepository.findFirstByNameAndUnitIdIsNull(AMP_ADMIN_ROLE_NAME);
		
		// check whether the user has a role assignment with AMP Admin
		boolean is = roleAssignmentRepository.existsByUserIdAndRoleIdAndUnitIdIsNull(user.getId(), admin.getId());
		String isstr = is ? "is" : "is not";
		
		log.info("The current user " + isstr + " " + AMP_ADMIN_ROLE_NAME);
		return is;
	}		
		
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getPermittedActions(List<ActionType>, List<TargetType>, List<Long>)
	 */
	@Override
	public List<UnitActions> getPermittedActions(List<ActionType> actionTypes, List<TargetType> targetTypes, List<Long> unitIds) {
		List<UnitActions> uas = new ArrayList<UnitActions>();
		
		// find all role assignments for the current user per given units ordered by unit ID
		AmpUser user = ampUserService.getCurrentUser();		
		List<RoleAssignment> ras = unitIds.isEmpty() ?
			roleAssignmentRepository.findByUserIdOrderByUnitId(user.getId()) : 
			roleAssignmentRepository.findByUserIdAndUnitIdInOrderByUnitId(user.getId(), unitIds);

		
		// for all the user's roles within each unit, merge the unique actions matching the actionTypes and/or targetTypes
		UnitActions ua = new UnitActions(0L, new ArrayList<Action>());	// current UnitActions
		for (RoleAssignment ra : ras) {
			Unit unit = ra.getUnit();		
			
			// skip global role assignment, where unit is null
			if (unit == null) {
				continue;
			}
			
			// when the current role assignment's unit ID is a new one
			Long unitId = unit.getId();
			if (!unitId.equals(ua.getUnitId())) {
				// if the previous UnitActions contains any actions, add it to the parent list
				if (!ua.getActions().isEmpty()) {
					uas.add(ua);
				}

				// start a new UnitAction as the current one
				ua = new UnitActions(unitId, new ArrayList<Action>());	
			}
			
			// merge the actions for the current role into current UnitActions  
			List<Action> actionsU = ua.getActions();
			Set<Action> actionsR = ra.getRole().getActions();
			for (Action action : actionsR) {
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
	
	
}
