package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
import edu.indiana.dlib.amppd.service.RoleService;
import edu.indiana.dlib.amppd.web.RoleAssignTable;
import edu.indiana.dlib.amppd.web.RoleAssignTuple;
import edu.indiana.dlib.amppd.web.RoleAssignUpdate;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of RoleService.
 * @author yingfeng
 */
@Service
@Slf4j
public class RoleServiceImpl implements RoleService {
	
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
	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.RoleService.getAssignableRoleLevel(Long)
	 */
	@Override
	public Integer getAssignableRoleLevel(Long unitId) {
		// get the current user
		AmpUser user = ampUserService.getCurrentUser();
		
		// find the highest role (i.e. with smallest role level) the user has
		Integer level = roleAssignmentRepository.findMinRoleLevelByUserIdAndUnitId(user.getId(), unitId);
		
		log.info("The role assignment level threshold (excluding) for the current user " + user.getUsername() + " in unit " + unitId + " is " + level);
		return level;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.RoleService.getAssignableRoles(Long)
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
	 * @see edu.indiana.dlib.amppd.service.RoleService.retrieveRoleAssignments(Long)
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
		RoleAssignTable response = new RoleAssignTable(level, roles, users, assignments);
		log.info("Successfully found " + users.size() + " users and " + nRoles + " roles for assignments in unit " + unitId);
		return response;
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.RoleService.updateRoleAssignments(Long, List<RoleAssignTuple>)
	 */
	@Override
	public RoleAssignUpdate updateRoleAssignments(Long unitId, List<RoleAssignTuple> assignments) {
		List<RoleAssignmentDto> rasAdd = new ArrayList<RoleAssignmentDto>();
		List<RoleAssignmentDto> rasDelete = new ArrayList<RoleAssignmentDto>();
		RoleAssignUpdate raUpdate = new RoleAssignUpdate(rasAdd, rasDelete);
		Unit unit = unitRepository.findById(unitId).orElseThrow(() -> new StorageException("Unit not found: " + unitId));
		Integer level = getAssignableRoleLevel(unitId);
		
		// process each assignment request 
		for (RoleAssignTuple assignment : assignments) {
			Long userId = assignment.getUserId();
			Long roleId = assignment.getRoleId();
			String username = assignment.getUserName();
			String roleName = assignment.getRoleName();
			AmpUser user = null;
			Role role = null;
			
			// verify that the user exists, based on user ID or username;
			if (userId != null) {
				user = ampUserRepository.findById(userId).orElse(null);
			}
			else if (StringUtils.isNotBlank(username)) {
				user = ampUserRepository.findFirstByUsername(username);
			}			
			if (user == null) {
				throw new StorageException("Could not find user: userId = " + userId + ", username = " + username);
			}
				
			// verify that the role exists, based on role ID or name plus unitId;
			if (roleId != null) {
				role = roleRepository.findById(roleId).orElse(null);
			}
			else if (StringUtils.isNotBlank(roleName)) {
				// check for global role first
				role = roleRepository.findFirstByNameAndUnitIdIsNull(roleName);
				// if not found, check for unit role
				role = role == null? roleRepository.findFirstByNameAndUnitId(roleName, unitId) : role;
			}			
			if (role == null) {
				throw new StorageException("Could not find role: roleId = " + roleId + ", roleName = " + roleName + " in unit " + unitId);
			}
							
			// verify that the current user is allowed to assign/unassign this role
			if (role.getLevel() <= level) {
				throw new RuntimeException("The current user is not allowed to assign/unassign role " + role.getName() + " to user " + user.getUsername() + " in unit " + unitId);
			}
			
			// retrieve role assignment based on userId, roleId, unitId
			RoleAssignment ra = roleAssignmentRepository.findFirstByUserIdAndRoleIdAndUnitId(user.getId(), role.getId(), unitId);

			// add assignment
			if (assignment.isAssigned()) {
				// if already assigned, give warning
				if (ra != null) {
					log.warn("No need to assign user " + userId + " with role " + roleId + " in unit " + unitId + " as RoleAssignment " + ra.getId() + " already exists!");
				}
				// otherwise create and save the new role assignment, and add it to the list-added
				else {
					ra = new RoleAssignment(user, role, unit);
					ra = roleAssignmentRepository.save(ra);
					RoleAssignmentDto radto = new RoleAssignmentDto(ra);
					rasAdd.add(radto);
				}
			}
			// delete assignment
			else {
				// if already unassigned, give warning
				if (ra == null) {
					log.warn("No need to unassign user " + userId + " from role " + roleId + " in unit " + unitId + " as this RoleAssignment doesn't exist!");
				}
				// otherwise delete the existing role assignment, and add it to the list-deleted
				else {
					roleAssignmentRepository.deleteById(ra.getId());
					RoleAssignmentDto radto = new RoleAssignmentDto(ra);
					rasDelete.add(radto);
				}
			}
		}
		
		log.info("Successfully added " + rasAdd.size() + " and deleted " + rasDelete.size() + " role assignments out of " + assignments.size() + " requests within unit " + unitId);		
		return raUpdate;
	}
	
}
;