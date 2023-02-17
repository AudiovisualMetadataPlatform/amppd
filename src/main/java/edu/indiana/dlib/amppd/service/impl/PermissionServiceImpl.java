package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
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
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAccessibleUnits()
	 */
	@Override
	public List<Unit> getAccessibleUnits() {
		List<Unit> units = new ArrayList<Unit>();

		// find all role assignments for the current user
		AmpUser user = ampUserService.getCurrentUser();		
		List<RoleAssignment> ras = roleAssignmentRepository.findByUserId(user.getId());
		
		// retrieve the associated units
		for (RoleAssignment ra : ras) {
			Unit unit = ra.getUnit();
			
			// if an assignment is not associated with any unit, it's a global one, which means the user has AMP Admin role and can access all units 
			if (unit == null) {
				Iterable<Unit> allunits = unitRepository.findAll();
				units = IterableUtils.toList(allunits);
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
		log.info("Current user " + user.getUsername() + " " + has + " permission to perform action " + action.getName() +" in unit " + unitId);				
		return has;
	}
	
	
}
