package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.repository.RoleAssignmentRepository;
import edu.indiana.dlib.amppd.repository.RoleRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.RoleService;
import edu.indiana.dlib.amppd.web.RoleAssignRequest;
import edu.indiana.dlib.amppd.web.RoleAssignResponse;
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
	public List<Role> getAssignableRoles(Long unitId) {
		// find the assignable role level threshold for the current user
		Integer level = getAssignableRoleLevel(unitId);
		
		// the user can only assign roles with lower level roles (i.e. with greater level)
		List<Role> roles = roleRepository.findLevelGreaterAndUnitIdIsOrNull(level, unitId);
		
		log.info("Successfully found " + roles.size() + " assignable roles for the current user in unit " + unitId);
		return roles;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.RoleService.retrieveRoleAssignments(Long)
	 */
	@Override
	public RoleAssignResponse retrieveRoleAssignments(Long unitId) {		
		// find the role assignment level limit for the current user in the unit
		Integer level = getAssignableRoleLevel(unitId);

		// get all available roles for the unit
		List<Role> roles = roleRepository.findByUnitIdIsNullOrIsOrderByLevel(unitId);
		
		// set up reverse hashmap for the roles to facilitate index lookup based on roleId
		HashMap<Long, Integer> mapRoles = new HashMap<Long, Integer>();	
		int i = 0;
		for (Role role : roles) {			
			mapRoles.put(role.getId(), i);			
		}
		
		// get all role assignments for the unit
		List<RoleAssignment> ras = roleAssignmentRepository.findByUnitIdOrderByUserId(unitId);
		
		// initialize users list and assignments array
		Long userIdC = 0L;
		List<AmpUser> users = new ArrayList<AmpUser>();
		List<Boolean[]> assignments = new ArrayList<Boolean[]>();
		int nRoles = roles.size();
		
		// go through role assignments ordered by user IDs, populate users list and users-roles assignment table
		for (RoleAssignment ra : ras) {
			AmpUser user = ra.getUser();
			Long userId = user.getId();

			// start a new user when userId changes
			if (!userId.equals(userIdC)) {
				userIdC = userId;
				users.add(user);
				assignments.add(new Boolean[nRoles]);
			}
			
			// populate assignments cell
			int row = assignments.size();
			int col = mapRoles.get(ra.getRole().getId());
			Boolean[] userRoles = assignments.get(row-1);
			userRoles[col] = true;
		}
		
		// generate the assignment table
		RoleAssignResponse response = new RoleAssignResponse(level, roles, users, assignments);
		log.info("Successfully found " + users.size() + " users and " + nRoles + " roles for assignment in unit " + unitId);
		return response;
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.RoleService.updateRoleAssignments(Long, List<RoleAssignRequest>)
	 */
	@Override
	public List<RoleAssignment> updateRoleAssignments(Long unitId, List<RoleAssignRequest> assignments) {
		List<RoleAssignment> ras = new ArrayList<RoleAssignment>();
		
		for (RoleAssignRequest assignment : assignments) {
			RoleAssignment ra = roleAssignmentRepository.updateByUserIdAndRoleIdAndUnitId(assignment.getUserId(), assignment.getRoleId(), unitId);
			ras.add(ra);
		}
		
		log.info("Successfully updated " + ras.size() + " role assignments within unit " + unitId);
		return ras;
	}
	
}
