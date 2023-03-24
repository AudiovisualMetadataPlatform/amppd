package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.bean.CsvToBeanBuilder;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.dto.ActionDto;
import edu.indiana.dlib.amppd.model.dto.RoleAction;
import edu.indiana.dlib.amppd.model.dto.RoleDto;
import edu.indiana.dlib.amppd.repository.ActionRepository;
import edu.indiana.dlib.amppd.repository.RoleRepository;
import edu.indiana.dlib.amppd.service.PermissionRefreshService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MgmRefreshService
 * @author yingfeng
 */
@Service
@Slf4j
public class PermissionRefreshServiceImpl implements PermissionRefreshService {
	
	public static final String DIR = "db";
	public static final String ROLE = "ac_role";
	public static final String ACTION = "ac_action";
	public static final String ROLE_ACTION = "ac_role_action";

	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;
	
	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private ActionRepository actionRepository;

//	@Autowired
//	private RoleAssignmentRepository roleAssignmentRepository;
//	
//	@Autowired
//	private AmpUserRepository ampUserRepository;
//	
//	@Autowired
//	private UnitRepository unitRepository;
	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MgmRefreshService.refreshMgmTables()
	 */
	@Override
    @Transactional
	public void refreshPermissionTables() {
		if (!amppdPropertyConfig.getRefreshPermissionTables()) return;
		
		List<Role> roles = refreshRole();
		refreshAction();
		refreshRoleAction(roles.size());		
//		initRoleAssignment();
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionRefreshService.refreshRole()
	 */
	@Override
    @Transactional
	public List<Role> refreshRole() {
		log.info("Start refreshing Role table ...");
		List<Role> roles = new ArrayList<Role>();
		
		// open ac_role.csv
		String filename = DIR + "/" + ROLE + ".csv"; 
		BufferedReader breader = null;
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Role table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of Role objects
		List<RoleDto> rolesCsv;
		try {
			rolesCsv = new CsvToBeanBuilder<RoleDto>(breader).withType(RoleDto.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Role table: invalid CSV format with " + filename, e);
		}
		
		// record the refresh start time
		Date refreshStart = new Date();
		
		// save each of the Role objects, either creating a new one or updating the existing one
		for (RoleDto roleCsv : rolesCsv) {			
			// note: we can't just save all roles directly, as that would create new records in the table; instead,
			// we need to find each existing record if any based on ID and update it, and create new one only if no existing one
			Role role = roleRepository.findFirstByNameAndUnitId(roleCsv.getName(), null);			
			if (role == null) {
				role = new Role();
			}
			roleCsv.copyTo(role);
			
			// it appears that if saving the same object to DB without any field value changed, modifiedDate won't be updated by Spring Data
			// thus, need to set it explicitly, in order to distinguish obsolete records.
			role.setModifiedDate(new Date());
			
			// add the saved role to roles list to return
			// Note that roleCsv and role could be different, the former may not have ID populated if it's a new role not existing in DB
			role = roleRepository.save(role);
			roles.add(role);	
		}		
		
		// delete all obsolete global roles, i.e. those with null unitId and not updated during this pass of refresh;
		// based on the assumption that the csv file includes all the roles we want to keep 
		List<Role> deletedRoles = roleRepository.deleteByUnitIdIsNullAndModifiedDateBefore(refreshStart);
		log.info("Deleted " + deletedRoles.size() + " obsolete global roles older than current refresh start time at " + refreshStart);			
		
		log.info("Successfully refreshed " + roles.size() + " global roles from " + filename);
		return roles;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionRefreshService.refreshAction()
	 */
	@Override
    @Transactional
	public List<Action> refreshAction() {
		log.info("Start refreshing Action table ...");
		List<Action> actions = new ArrayList<Action>();
		
		// open ac_action.csv
		String filename = DIR + "/" + ACTION + ".csv"; 
		BufferedReader breader = null;
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Action table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of Action objects
		List<ActionDto> actionsCsv;
		try {
			actionsCsv = new CsvToBeanBuilder<ActionDto>(breader).withType(ActionDto.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Action table: invalid CSV format with " + filename, e);
		}
		
		// record the refresh start time
		Date refreshStart = new Date();
		
		// save each of the Action objects, either creating a new one or updating the existing one
		for (ActionDto actionCsv : actionsCsv) {			
			// note: we can't just save all actions directly, as that would create new records in the table; instead, 
			// we need to find each existing record if any based on ID and update it, and create new one only if no existing one
			Action action = actionRepository.findFirstByName(actionCsv.getName());			
			if (action == null) {
				action = new Action();			
			}
			actionCsv.copyTo(action);
			
			// it appears that if saving the same object to DB without any field value changed, modifiedDate won't be updated by Spring Data
			// thus, need to set it explicitly, in order to distinguish obsolete records.
			action.setModifiedDate(new Date());

			// add the saved action to actions list to return
			// Note that actionCsv and action could be different, the former may not have ID populated if it's a new action not existing in DB
			action = actionRepository.save(action);
			actions.add(action);
		}		
		
		// delete all obsolete actions, i.e. those not updated during this pass of refresh;
		// based on the assumption that the csv file includes all the actions we want to keep 
		List<Action> deletedActions = actionRepository.deleteByModifiedDateBefore(refreshStart);
		log.info("Deleted " + deletedActions.size() + " obsolete actions older than current refresh start time at " + refreshStart);			
				
		log.info("Successfully refreshed " + actions.size() + " actions from " + filename);
		return actions;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionRefreshService.refresh(int)
	 */
	@Override
    @Transactional
	public List<RoleAction> refreshRoleAction(int roleDepth) {
		log.info("Start refreshing RoleAction table with depth of role hierarchy = " + roleDepth);
		
		// open ac_role_action.csv
		String filename = DIR + "/" + ROLE_ACTION + ".csv"; 
		BufferedReader breader = null;
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Role table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of RoleAction objects
		List<RoleAction> roleActions;
		try {
			roleActions = new CsvToBeanBuilder<RoleAction>(breader).withType(RoleAction.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh RoleAction table: invalid CSV format with " + filename, e);
		}
		
		// initialize roles records
		Map<String, Role> nroles = new HashMap<String, Role>();	// name-role map
		Role[] lroles = new Role[roleDepth];	// role array indexed by its level		
		
		// add actions in the parsed action list to each role in the parsed role list
		for (RoleAction roleAction : roleActions) {	
			String roleName = roleAction.getRoleName();
			Role role = nroles.get(roleName);
			
			// first encounter of the role in the role_action list
			if (role == null) {
				// make sure the role exists in DB
				role = roleRepository.findFirstByNameAndUnitId(roleName, null);				
				if (role == null) {
					throw new RuntimeException("Failed to refresh RoleAction table: Invalid roleAction with non-existing role name in CSV: " + roleAction.getRoleName());
				}
				
				// record role in name-role map
				nroles.put(roleName, role);
				
				// record role in level-role array, make sure the level is valid
				int level = role.getLevel();
				if (level < 0 || level > roleDepth - 1) {
					throw new RuntimeException("Failed to refresh RoleAction table: Invalid role level " + level + " for role " + roleAction.getRoleName());
				}
				lroles[level] = role;

				// reset the role action list to empty, to remove obsolete actions
				role.setActions(new HashSet<Action>());				
			}

			Action action = actionRepository.findFirstByName(roleAction.getActionName());
			if (action == null) {
				throw new RuntimeException("Failed to refresh RoleAction table: Invalid roleAction with non-existing action name in CSV: " + roleAction.getActionName());
			}
			 
			role.getActions().add(action);
		}
		
		// append lower level roles actions to higher level roles' actions, starting from lowest role, and persist each role
		for (int level = roleDepth-1; level >= 0; level--) {
			Role role = lroles[level];
			
			// make sure each level has a role
			if (role == null) {
				throw new RuntimeException("Failed to refresh RoleAction table: there is no role at level " + level + " in CSV.");				
			}
			
			// append actions from role one level below if within the array boundary
			if (level + 1 < roleDepth) {
				Role lrole = lroles[level+1];
				role.getActions().addAll(lrole.getActions());
			}
			
			// save current role
			roleRepository.save(role);
			log.debug("Refreshed role " + role + " with " + role.getActions().size() + " actions.");
		}		
		
		/* Note:
		 * RoleAction table is mapped by Spring Data and doesn't have DB audit fields such as lastUpdateDate; thus, 
		 * we can't use lastUpdateDate to identify obsolete rows to delete; furthermore, the permission table also contains 
		 * permissions dynamically assigned for unit-specific roles, which shouldn't be refreshed based on the csv file.
		 * On the other hand, the role_action.csv should contain permissions for all currently valid roles,
		 * and Spring Data shall take care of removing obsolete actions when the valid actions are saved for each role.
		 * Furthermore, obsolete roles and actions would have been removed during role/action table refreshing, along with 
		 * all associated permissions, so no further deletion on role_action table is needed here.		
		 */
		
		log.info("Successfully refreshed " + roleActions.size() + " roleActions for " + roleDepth + " roles from " + filename);
		return roleActions;
	}

//	/**
//	 * Put in some tmp test data.
//	 */
//	private void initRoleAssignment() {
//		final String AMP_ADMIN = "AMP Admin";
//		String[] usernames = {"ampadmin", "amppd@iu.edu", "yingfeng@iu.edu", "amppdiu@gmail.com"};
//		String[] roleNames = {AMP_ADMIN, "Unit Manager", "Collection Manager", "Unit Viewer"};
//		String unitName = "AMP Pilot Unit";
//		
//		Unit unitScope = unitRepository.findFirstByName(unitName);
//		int i = 0;
//		
//		for (String username : usernames) {
//			AmpUser user = ampUserRepository.findFirstByUsername(username);
//			Role role = roleRepository.findFirstByNameAndUnitId(roleNames[i++], null);
//			Unit unit = AMP_ADMIN.equals(role.getName()) ? null : unitScope;
//			Long unitId = unit == null ? null : unit.getId();
//
//			RoleAssignment ra = new RoleAssignment(user, role, unit);			
//			RoleAssignment existRa = this.roleAssignmentRepository.findFirstByUserIdAndRoleIdAndUnitId(user.getId(), role.getId(), unitId);
//			if (existRa == null) {
//				roleAssignmentRepository.save(ra);
//			}
//		}		
//	}
//	
}
