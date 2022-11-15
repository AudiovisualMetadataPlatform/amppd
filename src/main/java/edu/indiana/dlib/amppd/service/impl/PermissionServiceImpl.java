package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.repository.ActionRepository;
import edu.indiana.dlib.amppd.repository.RoleAssignmentRepository;
import edu.indiana.dlib.amppd.repository.RoleRepository;
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
	private RoleRepository roleRepository;

	@Autowired
	private ActionRepository actionRepository;

	@Autowired
	private RoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	private AmpUserService ampUserService;
	
	
	public boolean hasPermsion(ActionType actionType, TargetType targetType, Long unitId) {
		Action action = actionRepository.findFirstByActionTypeAndTargetType(actionType, targetType);
		
		boolean has = hasPermission(action, unitId);
		return has;
	}
	
	public boolean hasPermsion(HttpMethod httpMethod, String urlPattern, Long unitId) {
		Action action = actionRepository.findFirstByHttpMethodAndUrlPattern(httpMethod, urlPattern);
		
		boolean has = hasPermission(action, unitId);
		return has;
	}
	
	public boolean hasPermission(Action action, Long unitId) {
		boolean has = false;
		
		AmpUser user = ampUserService.getCurrentUser();
		Set<Role> roles = action.getRoles();		
		List<Long> roleIds = new ArrayList<Long>();
		
		for (Role role : roles) {
			roleIds.add(role.getId());
		}
		
		has = roleAssignmentRepository.existsByUserIdAndRoleIdInAndUnitId(user.getId(), roleIds, unitId);
		boolean hasGlobal = roleAssignmentRepository.existsByUserIdAndRoleIdInAndUnitIdIsNull(user.getId(), roleIds);
		has = has || hasGlobal;
				
		return has;
	}
	
}
