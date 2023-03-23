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

	@Value("#{'${amppd.unitRoles}'.split(',')}")
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
			unitRoleNames = getUnitRoleNames();							// all predefined unit role names
			actions = actionRepository.findByConfigurable(true);		// get configurable actions only
			roles = roleRepository.findByUnitIdOrderByLevel(unitId);	// get roles with actions within the unit
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
				log.error("Failed to update all " + roleActionsIds.size() + " requested " + scope + " roles configuration: Unit not found");
				return rolesUpdated;
			}
		}

		// update actions config for each role
		for (RoleActionsId raId : roleActionsIds) {
			Long roleId = raId.getId();
			String roleName = raId.getName();
			Role role = null;

			// either roleId or roleName must be provided; the former supersedes the latter if both provided
			if (roleId != null) {
				role = roleRepository.findById(roleId).orElse(null);
			}
			else if (StringUtils.isNotBlank(roleName)) {
				role = unitId == null ? roleRepository.findFirstByNameAndUnitIdIsNull(roleName) : roleRepository.findFirstByNameAndUnitId(roleName, unitId);
			}			

			// if role not found
			if (role == null ) {
				// if roleId is null but unitId and roleName are provided, that indicates a new unit role to be created
				if (roleId == null && unitId != null && StringUtils.isNotBlank(roleName)) {
					role = new Role();
					role.setName(roleName);
					role.setDescription(roleName);	// TODO use name as description for unit roles now, can be configurable in the future if desired
					role.setLevel(Role.MAX_LEVEL);	// unit roles all have max level
					role.setUnit(unit);				// associate the role with the given unit
					role.setRoleAssignements(new HashSet<RoleAssignment>()); // no assignment for new role
					log.info("Creating new " + scope + " role, roleName = " + roleName);				
				}
				// otherwise it's an invalid request, skip all its actions configuration with no update
				else {
					log.error("Failed to update " + scope + " role configuration: Role not found: roleId = " + roleId + ", roleName = " + roleName);				
					continue;					
				}
			}

			// otherwise the role exists, verify that it's configurable, i.e. it's not AMP Admin, skip the role if so
			roleId = role.getId();
			roleName = role.getName();
			String roleStr = scope + " role configuration (" + roleId + ": " + roleName + ")";
			if (Role.AMP_ADMIN_ROLE_NAME.equalsIgnoreCase(roleName)) {
				log.error("Failed to update " + roleStr + ": Role not configurable");				
				continue;									
			}
			
			// reset actions to empty whether or not this is a new role, as the whole set will be overwritten with the current role_actions config
			Set<Action> actions = new HashSet<Action>();
			role.setActions(actions);
			List<Long> actionIds = raId.getActionIds();
			
			// find and add the role's new actions by ID
			for (Long actionId : actionIds) {
				// verify that the action exist, 
				Action action = actionRepository.findById(actionId).orElse(null);				
				
				// if current action doesn't, abandon the rest of the actions to skip the role with no update
				if (action == null) {
					log.error("Failed to update " + roleStr + ": Action not found: actionId = " + actionId);				
					break;					
				}
				
				// if current action is not configurable, abandon the rest of the actions to skip the role with no update
				if (!action.getConfigurable()) {
					log.error("Failed to update " + roleStr + ": Action not configurable: actionId = " + actionId + ", actionName = " + action.getName());				
					break;					
				}
				
				// otherwise add the action to the role's action set
				actions.add(action);
			}
			
			// if above action loop was broken, i.e. not all actions are valid, skip the configuration of this role with no update, to ensure data integrity
			if (actions.size() < actionIds.size()) continue; 

			// otherwise save the role with all its actions, and add the updated role to return list			
			Role roleUpdated = roleRepository.save(role);
			RoleActionsDto raDto = new RoleActionsDto(roleUpdated);
			rolesUpdated.add(raDto);
			log.info("Successfully updated " + roleStr + " with " + actionIds.size() + " actions.");			
		}			

		log.info("Updated " + rolesUpdated.size() + " among all " + roleActionsIds.size() + " requested " + scope + "roles configuration.");
		return rolesUpdated;
	}
	
}
