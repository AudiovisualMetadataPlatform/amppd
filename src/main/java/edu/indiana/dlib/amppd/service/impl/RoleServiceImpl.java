package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.model.dto.RoleActionsDto;
import edu.indiana.dlib.amppd.model.dto.RoleActionsId;
import edu.indiana.dlib.amppd.model.projection.ActionBrief;
import edu.indiana.dlib.amppd.model.projection.RoleBriefActions;
import edu.indiana.dlib.amppd.repository.ActionRepository;
import edu.indiana.dlib.amppd.repository.RoleRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.RoleService;
import edu.indiana.dlib.amppd.web.RoleActionConfig;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RoleService.
 * @author yingfeng
 */
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

	@Autowired
	private ActionRepository actionRepository;	
	
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private UnitRepository unitRepository;	

	@Value("#{'${amppd.unitRolesProperty}'.split(',')}")
	private List<String> unitRolesProperty;
	

	/**
	 * @See edu.indiana.dlib.amppd.service.RoleService.getUnitRoleNames()
	 */
	@Override
	public List<String> getUnitRoleNames() {
		return unitRolesProperty; 
	}
	
	/**
	 * @See edu.indiana.dlib.amppd.service.RoleService.retrieveRoleActionConfig(Long)
	 */
	@Override
	public RoleActionConfig retrieveRoleActionConfig(Long unitId) {
		String scope;
		List<String> unitRoleNames;
		List<RoleBriefActions> roles; 
		List<ActionBrief> actions;
		
		// for global config
		if (unitId == null) {
			scope = "global";
			unitRoleNames= new ArrayList<String>();						// no need for unitRoleNames
			actions = actionRepository.findBy();						// get both configurable and non-configurable actions
			roles = roleRepository.findByUnitIdIsNullOrderByLevel();	// get all global roles with actions
		}
		// for unit-scope config
		else {
			scope = "unit " + unitId;
			unitRoleNames = unitRolesProperty;							// all predefined unit role names
			actions = actionRepository.findByConfigurable(true);		// get configurable actions only
			roles = roleRepository.findByUnitIdOrderByLevel(unitId);	// get roles with actions for the unit
		}
		
		RoleActionConfig raConfig = new RoleActionConfig(unitRoleNames, roles, actions);
		log.info("Successfully retrieved " + roles.size() + " roles and " + actions.size() + " actions for " + scope + " role_action configuration.");
		return raConfig;
	}
	
	/**
	 * @See edu.indiana.dlib.amppd.service.RoleService.updateRoleActionConfig(Long, List<RoleActionsId>)
	 */
	@Override
	public List<RoleActionsDto> updateRoleActionConfig(Long unitId, List<RoleActionsId> roleActionsIds) {
		List<RoleActionsDto> rolesUpdated = new ArrayList<RoleActionsDto>();
		String scope;
		Unit unit = null;
		
		// for global config, unit is null
		if (unitId == null) {
			scope = "global";
		}
		// for unit-scope config, verify that unit exists
		else {
			scope = "unit " + unitId;
			unit = unitRepository.findById(unitId).orElse(null);
			if (unit == null) {
				log.error("Failed to update all " + roleActionsIds.size() + " roles with actions for " + scope + " role_action configuration: Unit not found");
				return rolesUpdated;
			}
		}

		// update actions config for each role
		for (RoleActionsId ra : roleActionsIds) {
			Long roleId = ra.getId();
			String roleName = ra.getName();
			Role role = null;

			// either roleId or roleName must be provided; the former supersedes the latter if both provided
			if (roleId != null) {
				role = roleRepository.findById(roleId).orElse(null);
			}
			else if (StringUtils.isNotBlank(roleName)) {
				role = unitId == null ? roleRepository.findFirstByNameAndUnitIdIsNull(roleName) : roleRepository.findFirstByNameAndUnitId(roleName, unitId);
			}			

			// if role not found but roleId was provided, that indicates invalid role, skip all its actions configuration with no update
			if (role == null && roleId != null) {
				log.error("Failed to update role with actions: Role not found: roleId = " + roleId + ", roleName = " + roleName + ", unitId = " + unitId);				
				continue;
			}
			
			// otherwise if role and roleId are null but unitId and roleName are provided, that indicates a new unit role to be created
			if (role == null && roleId == null && unitId != null && StringUtils.isNotBlank(roleName)) {
				role = new Role();
				role.setName(roleName);
				role.setDescription(roleName);	// TODO use name as description for unit roles now, can be configurable as well if desired
				role.setLevel(Role.MAX_LEVEL);	// unit roles all have max level
				role.setUnit(unit);				// associate with the current unit
				role.setRoleAssignements(new HashSet<RoleAssignment>()); // no assignment for new role
				log.info("Creating new role: roleName = " + roleName + ", unitId = " + unitId);				
			}
			
			// reset actions to empty whether or not this is a new role, as the whole set will be rolesUpdated with current role_actions config
			Set<Action> actions = new HashSet<Action>();
			role.setActions(actions);
			List<Long> actionIds = ra.getActionIds();
			
			// find and add the role's new actions by ID
			for (Long actionId : actionIds) {
				// verify that the action exist, 
				Action action = actionRepository.findById(actionId).orElse(null);				
				
				// if one action of the role is invalid, abandon all rest of the actions to skip the role with no update
				if (action == null) {
					log.error("Failed to update role with actions: Role not found: roleId = " + roleId + ", roleName = " + roleName + ", unit = " + unitId);				
					break;					
				}
				
				// otherwise add the action to the role's action set
				actions.add(action);
			}
			
			// if above action loop was broken, i.e. not all actions are valid, skip the configuration of this role with no update, to ensure data integrity
			if (actions.size() < actionIds.size()) continue; 

			// otherwise save the role with all its actions and data, and add the rolesUpdated role to return list			
			Role roleUpdated = roleRepository.save(role);
			RoleActionsDto raDto = new RoleActionsDto(roleUpdated);
			log.debug("Successfully rolesUpdated " + scope + " role " + raDto.getId() + " with " + actionIds.size() + " actions.");			
		}			

		log.info("Updated " + rolesUpdated.size() + " roles with actions " + " among all " + roleActionsIds.size() + " requested for " + scope + " role_action configuration.");
		return rolesUpdated;
	}
	
}
