package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.model.dto.AmpUserDto;
import edu.indiana.dlib.amppd.model.dto.RoleDto;
import edu.indiana.dlib.amppd.repository.RoleAssignmentRepository;
import edu.indiana.dlib.amppd.repository.RoleRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
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
	private UnitRepository unitRepository;	
	
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
	public List<RoleDto> getAssignableRoles(Long unitId) {
		// find the assignable role level threshold for the current user
		Integer level = getAssignableRoleLevel(unitId);
		
		// the user can only assign roles with lower level roles (i.e. with greater level)
		List<RoleDto> roles = roleRepository.findAssignableRolesInUnit(level, unitId);
		
		log.info("Successfully found " + roles.size() + " assignable roles for the current user in unit " + unitId);
		return roles;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.RoleService.retrieveRoleAssignments(Long)
	 */
	@Override
	public RoleAssignResponse retrieveRoleAssignments(Long unitId) {		
		// find the role assignment level threshold for the current user in the unit, roles with greater levels above the threshold are assignable
		Integer level = getAssignableRoleLevel(unitId);

		// get all viewable roles for the unit
		List<RoleDto> roles = roleRepository.findViewableRolesInUnit(unitId);
		
		// set up reverse hashmap for the roles to facilitate index lookup based on roleId
		HashMap<Long, Integer> mapRoles = new HashMap<Long, Integer>();	
		int i = 0;
		for (RoleDto role : roles) {			
			mapRoles.put(role.getId(), i++);			
		}
		
		// get all role assignments for the unit
		List<RoleAssignment> ras = roleAssignmentRepository.findByUnitIdOrderByUserId(unitId);
		
		// initialize users list and assignments array
		int nRoles = roles.size();
		List<AmpUserDto> users = new ArrayList<AmpUserDto>();
		List<boolean[]> assignments = new ArrayList<boolean[]>();
		
		// go through role assignments ordered by user IDs, populate users list and users-roles assignment table
		for (RoleAssignment ra : ras) {
			int row = users.size() - 1;
			AmpUserDto userDto = row < 0 ? null : users.get(row);
			AmpUser user = ra.getUser();

			// start a new user when userId changes
			if (userDto == null || !userDto.getId().equals(user.getId())) {				
				userDto = new AmpUserDto(user);
				users.add(userDto);
				// initialize the current user-roles assignment boolean array with cells are all false
				assignments.add(new boolean[nRoles]); 
			}
			
			// populate current assignment cell			
			int col = mapRoles.get(ra.getRole().getId());
			boolean[] userRoles = assignments.get(row);
			userRoles[col] = true;
		}
		
		// generate the assignment table
		RoleAssignResponse response = new RoleAssignResponse(level, roles, users, assignments);
		log.info("Successfully found " + users.size() + " users and " + nRoles + " roles for assignments in unit " + unitId);
		return response;
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.RoleService.updateRoleAssignments(Long, List<RoleAssignRequest>)
	 */
	@Override
	public ImmutablePair<List<RoleAssignment>, List<RoleAssignment>> updateRoleAssignments(Long unitId, List<RoleAssignRequest> assignments) {
		List<RoleAssignment> rasAdd = new ArrayList<RoleAssignment>();
		List<RoleAssignment> rasDelete = new ArrayList<RoleAssignment>();
		Unit unit = unitRepository.findById(unitId).orElseThrow(() -> new StorageException("Unit not found: " + unitId));;
		
		// process each assignment request 
		for (RoleAssignRequest assignment : assignments) {
			Long userId = assignment.getUserId();
			Long roleId = assignment.getRoleId();
			RoleAssignment ra = roleAssignmentRepository.findFirstByUserIdAndRoleIdAndUnitId(userId, roleId, unitId);
			
			// add assignment
			if (assignment.isAssigned()) {
				// if already assigned, give warning
				if (ra != null) {
					log.warn("No need to assign user " + userId + " with role " + roleId + " in unit " + unitId + " as RoleAssignment " + ra.getId() + " already exists!");
				}
				// otherwise create and save the new role assignment, and add the saved one to the return list
				else {
					AmpUser user = ampUserService.getUserById(userId);
					Role role = roleRepository.findById(roleId).orElseThrow(() -> new StorageException("Role not found: " + roleId));;
					ra = new RoleAssignment(user, role, unit);
					ra = roleAssignmentRepository.save(ra);
					rasAdd.add(ra);
				}
			}
			// delete assignment, and add the deleted one to the return list
			else {
				if (ra == null) {
					log.warn("No need to unassign user " + userId + " from role " + roleId + " in unit " + unitId + " as this RoleAssignment doesn't exist!");
				}
				else {
					roleAssignmentRepository.delete(ra);
					rasDelete.add(ra);
				}
			}
		}
		
		ImmutablePair<List<RoleAssignment>, List<RoleAssignment>> ras = ImmutablePair.of(rasAdd, rasDelete);
		log.info("Successfully added " + rasAdd.size() + " and deleted " + rasDelete.size() + " role assignments out of " + assignments.size() + " requests within unit " + unitId);		
		return ras;
	}
	
}
