package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.model.dto.RoleAssignmentDto;
import edu.indiana.dlib.amppd.model.projection.AmpUserBrief;
import edu.indiana.dlib.amppd.model.projection.RoleAssignmentDetail;
import edu.indiana.dlib.amppd.model.projection.RoleBrief;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.RoleAssignmentRepository;
import edu.indiana.dlib.amppd.repository.RoleRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.RoleAssignService;
import edu.indiana.dlib.amppd.web.RoleAssignTable;
import edu.indiana.dlib.amppd.web.RoleAssignTuple;
import edu.indiana.dlib.amppd.web.RoleAssignUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RoleAssignService.
 * @author yingfeng
 */
@Service
@Slf4j
public class RoleAssignServiceImpl implements RoleAssignService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private RoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	private UnitRepository unitRepository;	

	@Autowired
	private AmpUserRepository ampUserRepository;

	@Autowired
	private AmpUserService ampUserService;

	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;


	/**
	 * @see edu.indiana.dlib.amppd.service.RoleAssignService.isAdmin()
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
	 * @see edu.indiana.dlib.amppd.service.RoleAssignService.assignAdminRole(AmpUser)
	 */
	@Override
	public RoleAssignment assignAdminRole(AmpUser user) {	
		// verify user
		String roleName = Role.AMP_ADMIN_ROLE_NAME;
		if (user == null) {
			throw new StorageException("Failed to assign user with role " + roleName + ": user is null.");			
		}		
		String username = user.getUsername();
		
		// find AMP Admin role
		Role role = roleRepository.findFirstByNameAndUnitIdIsNull(roleName);
		if (role == null) {
			throw new StorageException("Failed to assign user " + username + " with role " + roleName + ": role not found.");
		}

		// if the user is already assigned with AMP Admin role, no action needed
		RoleAssignment ra = roleAssignmentRepository.findFirstByUserIdAndRoleIdAndUnitIdNull(user.getId(), role.getId());
		if (ra != null) {
			log.info("User " + username + " is already assigned with role " + roleName);
			return ra;
		}
		
		// otherwise add new role assignment
		ra = new RoleAssignment(user, role, null);
		ra = roleAssignmentRepository.save(ra);
		
		log.info("Successfully asssigned user " + username + " with role " + roleName + " at RoleAssignment " + ra.getId());
		return ra;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.RoleAssignService.getAssignableRoleLevel(Long)
	 */
	@Override
	public Integer getAssignableRoleLevel(Long unitId) {
		// get the current user
		AmpUser user = ampUserService.getCurrentUser();

		// find the highest role (i.e. with smallest role level) the user has
		Integer level = roleAssignmentRepository.findMinRoleLevelByUserIdAndUnitId(user.getId(), unitId);

		// if current user doesn't have any role, or if his role level is higher than the role assignment level threshold, set his level to max, so he can't assign any role
		Integer threshold = amppdPropertyConfig.getRoleAssignmentMaxLevel();
		if (level == null || level > threshold) {
			level = Role.MAX_LEVEL;
		}

		log.info("The role assignment level threshold (excluding) for the current user " + user.getUsername() + " in unit " + unitId + " is " + level);
		return level;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.RoleAssignService.getAssignableRoles(Long)
	 */
	@Override
	public List<RoleBrief> getAssignableRoles(Long unitId) {
		// find the assignable role level threshold for the current user
		Integer level = getAssignableRoleLevel(unitId);

		// the user can only assign roles with lower level roles (i.e. with greater level)
		List<RoleBrief> roles = roleRepository.findAssignableRolesInUnit(level, unitId);

		log.info("Successfully found " + roles.size() + " assignable roles for the current user in unit " + unitId);
		return roles;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.RoleAssignService.retrieveRoleAssignments(Long)
	 */
	@Override
	public RoleAssignTable retrieveRoleAssignments(Long unitId) {		
		// find the role assignment level threshold for the current user in the unit, roles with greater levels above the threshold are assignable
		Integer level = getAssignableRoleLevel(unitId);

		// get all viewable roles for the unit
		List<RoleBrief> roles = roleRepository.findViewableRolesInUnit(unitId);

		// set up reverse hashmap for the roles to facilitate index lookup based on roleId
		HashMap<Long, Integer> mapRoles = new HashMap<Long, Integer>();	
		int i = 0;
		for (RoleBrief role : roles) {			
			mapRoles.put(role.getId(), i++);			
		}

		// get all role assignments for the unit
		List<RoleAssignmentDetail> ras = roleAssignmentRepository.findByUnitIdOrderByUserId(unitId);

		// initialize users list and assignments array
		int nRoles = roles.size();
		List<AmpUserBrief> users = new ArrayList<AmpUserBrief>();
		List<boolean[]> assignments = new ArrayList<boolean[]>();

		// go through role assignments ordered by user IDs, populate users list and users-roles assignment table
		for (RoleAssignmentDetail ra : ras) {
			int row = users.size() - 1;
			AmpUserBrief userLast = row < 0 ? null : users.get(row);
			AmpUserBrief user = ra.getUser();

			// start a new user when userId changes
			if (userLast == null || !userLast.getId().equals(user.getId())) {				
				userLast = user;
				users.add(userLast);
				// initialize the current user-roles assignment boolean array with cells all false
				assignments.add(new boolean[nRoles]); 
				row++;
			}

			// populate current assignment cell			
			int col = mapRoles.get(ra.getRoleId());
			boolean[] userRoles = assignments.get(row);
			userRoles[col] = true;
		}

		// generate the assignment table
		RoleAssignTable rat = new RoleAssignTable(level, roles, users, assignments);
		log.info("Successfully retrieved " + users.size() + " users and " + nRoles + " roles for assignments in unit " + unitId);
		return rat;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.RoleAssignService.updateRoleAssignments(Long, List<RoleAssignTuple>)
	 */
	@Override
	public RoleAssignUpdate updateRoleAssignments(Long unitId, List<RoleAssignTuple> assignments) {
		// initialize assignment results
		List<RoleAssignmentDto> added = new ArrayList<RoleAssignmentDto>();
		List<RoleAssignmentDto> deleted = new ArrayList<RoleAssignmentDto>();
		List<RoleAssignTuple> unchanged = new ArrayList<RoleAssignTuple>();
		List<RoleAssignTuple> failed = new ArrayList<RoleAssignTuple>();
		RoleAssignUpdate rau = new RoleAssignUpdate(added, deleted, unchanged, failed);
		boolean valid = true;

		// verify that unit exists
		Unit unit = unitRepository.findById(unitId).orElse(null);
		if (unit == null) {
			log.error("Unit not found: " + unitId);
			valid = false;
		}

		// verify that the current user can assign any role at all 
		Integer level = getAssignableRoleLevel(unitId);
		if (level.equals(Role.MAX_LEVEL)) {
			// TODO return 403 response header
			log.error("The current user is not allowed to assign/unassign any role.");
			valid = false;
		}

		// if unit and/or user level are invalid, all assignments fail 
		if (!valid) {
			failed.addAll(assignments);
		}
		// otherwise process each assignment request 
		else {
			for (RoleAssignTuple assignment : assignments) {
				Long userId = assignment.getUserId();
				Long roleId = assignment.getRoleId();
				String username = assignment.getUsername();
				String roleName = assignment.getRoleName();
				AmpUser user = null;
				Role role = null;

				// either userId or username must be provided; the former supersedes the latter if both provided
				if (userId != null) {
					user = ampUserRepository.findById(userId).orElse(null);
				}
				else if (StringUtils.isNotBlank(username)) {
					user = ampUserRepository.findFirstByUsername(username);
				}	

				// verify that the user exists and is active, based on user ID or username;
				if (user == null || !user.isActive()) {
					log.error("User not found or inactive: userId = " + userId + ", username = " + username);
					failed.add(assignment);
					continue;
				}

				// either roleId or roleName must be provided; the former supersedes the latter if both provided
				if (roleId != null) {
					role = roleRepository.findById(roleId).orElse(null);
				}
				else if (StringUtils.isNotBlank(roleName)) {
					// check for global role first
					role = roleRepository.findFirstByNameAndUnitIdIsNull(roleName);
					// if not found, check for unit role
					role = role == null? roleRepository.findFirstByNameAndUnitId(roleName, unitId) : role;
				}			

				// verify that the role exists, based on role ID or name plus unitId
				if (role == null) {
					log.error("Role not found: roleId = " + roleId + ", roleName = " + roleName + ", in unit " + unitId);
					failed.add(assignment);
					continue;
				}

				// record user/role id/name
				userId = user.getId();
				username = user.getUsername();
				roleId = role.getId();
				roleName = role.getName();

				// verify that the current user is allowed to assign/unassign this role, based on user's min role level and current role's level
				int rlevel = role.getLevel();
				if (rlevel <= level) {
					// TODO return 403 response header
					log.error("The current user at level " + level + " is not allowed to assign/unassign role " + role.getName() + " at level " + rlevel + " to user " + username + " in unit " + unitId);
					failed.add(assignment);
					continue;
				}

				// retrieve role assignment based on userId, roleId, unitId
				RoleAssignment ra = roleAssignmentRepository.findFirstByUserIdAndRoleIdAndUnitId(user.getId(), role.getId(), unitId);

				// add assignment
				if (assignment.isAssigned()) {
					// if already assigned, no need to update
					if (ra != null) {
						log.warn("No need to assign user " + username + " with role " + roleName + " in unit " + unitId + " as RoleAssignment " + ra.getId() + " already exists!");
						unchanged.add(assignment);
					}
					// otherwise create and save the new role assignment
					else {
						ra = new RoleAssignment(user, role, unit);
						ra = roleAssignmentRepository.save(ra);
						RoleAssignmentDto radto = new RoleAssignmentDto(ra);
						added.add(radto);
						log.debug("Asssigned user " + username + " with role " + roleName + " in unit " + unitId + " at RoleAssignment " + ra.getId());
					}
				}
				// delete assignment
				else {
					// if already unassigned, no need to update
					if (ra == null) {
						log.warn("No need to unassign user " + username + " from role " + roleName + " in unit " + unitId + " as such RoleAssignment doesn't exist!");
						unchanged.add(assignment);
					}
					// otherwise delete the existing role assignment
					else {
						roleAssignmentRepository.deleteById(ra.getId());
						RoleAssignmentDto radto = new RoleAssignmentDto(ra);
						deleted.add(radto);
						log.debug("Unasssigned user " + username + " with role " + roleName + " in unit " + unitId + " at RoleAssignment " + ra.getId());
					}
				}
			}
		}

		log.info("Role assignment results within unit " + unitId + ": " + added.size() + " added, " + deleted.size() + " deleted, " + unchanged.size() + " unchanged, " + failed.size() + " failed.");		
		return rau;
	}

}
;