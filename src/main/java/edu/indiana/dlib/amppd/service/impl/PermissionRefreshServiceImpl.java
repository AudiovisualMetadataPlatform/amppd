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

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.ac.RoleAction;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.repository.ActionRepository;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.RoleAssignmentRepository;
import edu.indiana.dlib.amppd.repository.RoleRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
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
	private RoleRepository roleRepository;

	@Autowired
	private ActionRepository actionRepository;

	@Autowired
	private RoleAssignmentRepository roleAssignmentRepository;
	
	@Autowired
	private AmpUserRepository ampUserRepository;
	
	@Autowired
	private UnitRepository unitRepository;
	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MgmRefreshService.refreshMgmTables()
	 */
	@Override
    @Transactional
	public void refreshPermissionTables() {
		List<Role> roles = refreshRole();
		List<Action> actions = refreshAction();
		refreshRoleAction(roles, actions);		
		initRoleAssignment();
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionRefreshService.refreshRole()
	 */
	@Override
    @Transactional
	public List<Role> refreshRole() {
		log.info("Start refreshing Role table ...");
		List<Role> roles = new ArrayList<Role>();
		String filename = DIR + "/" + ROLE + ".csv"; 
		BufferedReader breader = null;
		
		// open ac_role.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Role table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of Role objects
		try {
			roles = new CsvToBeanBuilder<Role>(breader).withType(Role.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Role table: invalid CSV format with " + filename, e);
		}
		
		// record the refresh start time
		Date refreshStart = new Date();
		
		// save each of the Role objects, either creating a new one or updating the existing one
		for (Role role : roles) {			
			// note: we can't just save all roles directly, as that would create new records in the table;
			// instead, we need to find each existing record if any based on ID and update it
			Role existRole = roleRepository.findFirstByNameAndUnitId(role.getName(), null);			
			if (existRole != null) {
				role.setId(existRole.getId());				
			}
			roleRepository.save(role);	
		}		
		
		// delete all obsolete roles, i.e. those not updated during this pass of refresh;
		// based on the assumption that the csv file includes all the roles we want to keep 
		List<Role> deletedRoles = roleRepository.deleteByModifiedDateBefore(refreshStart);
		log.info("Deleted " + deletedRoles.size() + " obsolete roles older than current refresh start time at " + refreshStart);			
		
		log.info("Successfully refreshed " + roles.size() + " roles from " + filename);
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
		String filename = DIR + "/" + ACTION + ".csv"; 
		BufferedReader breader = null;
		
		// open ac_action.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Action table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of Action objects
		try {
			actions = new CsvToBeanBuilder<Action>(breader).withType(Action.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Action table: invalid CSV format with " + filename, e);
		}
		
		// record the refresh start time
		Date refreshStart = new Date();
		
		// save each of the Action objects, either creating a new one or updating the existing one
		for (Action action : actions) {			
			// note: we can't just save all actions directly, as that would create new records in the table;
			// instead, we need to find each existing record if any based on ID and update it
			Action existAction = actionRepository.findFirstByName(action.getName());			
			if (existAction != null) {
				action.setId(existAction.getId());				
			}
			actionRepository.save(action);	
		}		
		
		// delete all obsolete actions, i.e. those not updated during this pass of refresh;
		// based on the assumption that the csv file includes all the actions we want to keep 
		List<Action> deletedActions = actionRepository.deleteByModifiedDateBefore(refreshStart);
		log.info("Deleted " + deletedActions.size() + " obsolete actions older than current refresh start time at " + refreshStart);			
				
		log.info("Successfully refreshed " + actions.size() + " actions from " + filename);
		return actions;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionRefreshService.refreshRoleAction()
	 */
	@Override
    @Transactional
	public void refreshRoleAction(List<Role> roles, List<Action> actions) {
		log.info("Start refreshing RoleAction table ...");
		List<RoleAction> roleActions = new ArrayList<RoleAction>();
		String filename = DIR + "/" + ROLE_ACTION + ".csv"; 
		BufferedReader breader = null;
		
		// open ac_role_action.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Role table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of RoleAction objects
		try {
			roleActions = new CsvToBeanBuilder<RoleAction>(breader).withType(RoleAction.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh RoleAction table: invalid CSV format with " + filename, e);
		}
		
		Map<Long, Role> existRoles = new HashMap<Long, Role>();
		
		// add all actions to  each role
		for (RoleAction roleAction : roleActions) {	
			Role role = roleRepository.findFirstByNameAndUnitId(roleAction.getRoleName(), null);
			if (role == null) {
				throw new RuntimeException("Failed to refresh RoleAction table: Invalid roleAction with non-existing role name in CSV: " + role.getName());
			}
			
			Action action = actionRepository.findFirstByName(roleAction.getActionName());
			if (action == null) {
				throw new RuntimeException("Failed to refresh ActionAction table: Invalid roleAction with non-existing action name in CSV: " + roleAction.getActionName());
			}
			 
			Role existRole = existRoles.get(role.getId());
			if (existRole == null) {
				existRole = role;
				existRoles.put(role.getId(), existRole);
				if (existRole.getActions() == null) {
					existRole.setActions(new HashSet<Action>());
				}
			}
			existRole.getActions().add(action);
		}
		
		// save all roles with associated actions
		for (Role role : existRoles.values()) {
			roleRepository.save(role);
		}
		
		/* TODO
		 * RoleAction table is mapped by Spring Data and doesn't have DB audit fields such as lastUpdateDate.
		 * We need to figure out another way to delete obsolete rows from this table, i.e.
		 * any row in the table that isn't represented by any csv row should be deleted 
		 */
		
		log.info("Successfully refreshed " + roleActions.size() + " roleActions from " + filename);
	}

	/**
	 * Put in some tmp test data.
	 */
	private void initRoleAssignment() {
		String[] usernames = {"ampadmin", "yingfeng@iu.edu", "mcwhitak@iu.edu", "ghoshar@iu.edu"};
		String[] roleNames = {"AMP Admin", "Unit Manager", "Collection Manager", "Unit Viewer"};
		String unitName = "AMP Pilot Unit";
		
		Unit unit = unitRepository.findFirstByName(unitName);
		int i = 0;
		
		for (String username : usernames) {
			AmpUser user = ampUserRepository.findFirstByUsername(username);
			Role role = roleRepository.findFirstByNameAndUnitId(roleNames[i++], null);
			RoleAssignment ra = new RoleAssignment(user, role, unit);
			
			RoleAssignment existRa = this.roleAssignmentRepository.findFirstByUserIdAndRoleIdAndUnitId(user.getId(), role.getId(), unit.getId());
			if (existRa == null) {
				roleAssignmentRepository.save(ra);
			}
		}		
	}
	
}
