package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.projection.ActionBrief;
import edu.indiana.dlib.amppd.model.projection.RoleAssignmentDetailActions;
import edu.indiana.dlib.amppd.model.projection.UnitBrief;
import edu.indiana.dlib.amppd.repository.ActionRepository;
import edu.indiana.dlib.amppd.repository.RoleAssignmentRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.service.RoleAssignService;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.UnitActions;
import edu.indiana.dlib.amppd.web.WorkflowResultFilterUnit;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;
import lombok.extern.slf4j.Slf4j;


/**
 * Implementation of PermissionService.
 * @author yingfeng
 */
@Service("permissionService")
@Slf4j
public class PermissionServiceImpl implements PermissionService {	
	@Autowired
	private UnitRepository unitRepository;

	@Autowired
	private WorkflowResultRepository workflowResultRepository;

	@Autowired
	private ActionRepository actionRepository;

	@Autowired
	private RoleAssignmentRepository roleAssignmentRepository;

	@Autowired
	private AmpUserService ampUserService;
		
	@Autowired
	private RoleAssignService roleAssignService;

	@Autowired
	private DataentityService dataentityService;
		

	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.hasReadPermission(Long, Class)
	 */
	@Override
	@Transactional
	public boolean hasReadPermission(Long id, Class clazz) {
		Long acUnitId = getAcUnitId(id, clazz);
		TargetType targetType = Supplement.class.isAssignableFrom(clazz) ? TargetType.Supplement : Action.TargetType.valueOf(clazz.getSimpleName());
		boolean has = hasPermission(ActionType.Read, targetType, acUnitId);
		return has;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.hasPermission(ActionType, TargetType, Long)
	 */
	@Override
	public boolean hasPermission(ActionType actionType, TargetType targetType, Long unitId) {
		Action action = actionRepository.findFirstByActionTypeAndTargetType(actionType, targetType);		
		boolean has = hasPermission(action, unitId);
		return has;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.hasPermission(HttpMethod, String, Long)
	 */
	@Override
	public boolean hasPermission(HttpMethod httpMethod, String urlPattern, Long unitId) {
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
		
		boolean has = false;
		String ustr = "";

		// if unitId is provided, check permission within that unit
		if (unitId != null) {
			// check if the current user is assigned to one of the above roles
			// the only case when role assignment unit is null is for AMP Admin, who has permission for all actions;
			// otherwise the role assignment must be associated with some unit
			has = roleAssignmentRepository.existsByUserIdAndRoleIdInAndUnitIdIsNull(user.getId(), roleIds);
			has = has || roleAssignmentRepository.existsByUserIdAndRoleIdInAndUnitId(user.getId(), roleIds, unitId);
			ustr = " in unit " + unitId + ".";
		}
		// otherwise check if user has such roles in any unit at all 
		else {
			has = roleAssignmentRepository.existsByUserIdAndRoleIdIn(user.getId(), roleIds);
			ustr = " in at least one of the units.";
		}

		String hasstr = has ? "has" : "has no";
		log.info("Current user " + user.getUsername() + " " + hasstr + " permission to perform action " + action.getName() + ustr);				
		return has;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getPermittedActions(List<ActionType>, List<TargetType>, List<Long>)
	 */
	@Override
	public List<UnitActions> getPermittedActions(List<ActionType> actionTypes, List<TargetType> targetTypes, List<Long> unitIds) {
		List<UnitActions> uas = new ArrayList<UnitActions>();
		
		// find all role assignments for the current user per given units ordered by unit ID
		AmpUser user = ampUserService.getCurrentUser();		
		List<RoleAssignmentDetailActions> ras = unitIds.isEmpty() ?
			roleAssignmentRepository.findByUserIdOrderByUnitId(user.getId()) : 
			roleAssignmentRepository.findByUserIdAndUnitIdInOrderByUnitId(user.getId(), unitIds);
		
		// for all the user's roles within each unit, merge the unique actions matching the actionTypes and/or targetTypes
		UnitActions ua = new UnitActions(0L, new HashSet<ActionBrief>());	// current UnitActions
		for (RoleAssignmentDetailActions ra : ras) {
			Long unitId = ra.getUnitId();		
			
			// skip global role assignment, where unit is null
			if (unitId == null) {
				continue;
			}
			
			// when the current role assignment's unit ID is a new one
			if (!unitId.equals(ua.getUnitId())) {
				// if the previous UnitActions contains any actions, add it to the parent list
				if (!ua.getActions().isEmpty()) {
					uas.add(ua);
				}

				// start a new UnitAction as the current one
				ua = new UnitActions(unitId, new HashSet<ActionBrief>());	
			}
			
			// merge the actions for the current role into current UnitActions  
			Set<ActionBrief> actionsU = ua.getActions();
			Set<ActionBrief> actionsR = ra.getRole().getActions();
			for (ActionBrief action : actionsR) {
				// the current action must be unique, i.e. not already added to the UnitAction list yet
				boolean match = !actionsU.contains(action);
				
				// if actionTypes is specified, the current actionType must be one of them
				if (!actionTypes.isEmpty()) {
					match = match && actionTypes.contains(action.getActionType());
				}

				// if targetTypes is specified, the current targetType must be one of them
				if (!targetTypes.isEmpty()) {
					match = match && targetTypes.contains(action.getTargetType());
				}
				
				// add the action if all criteria satisfy
				if (match) {
					actionsU.add(action);
				}
			}			
		}
			
		// add the last UnitActions to the parent list if containing any actions
		if (!ua.getActions().isEmpty()) {
			uas.add(ua);
		}		
		
		log.info("Successfully found all permitted actions in " + uas.size() + " units for the current user " + user.getUsername());
		return uas;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAccessibleUnits()
	 */
	@Override
	public Set<UnitBrief> getAccessibleUnits() {
		Set<UnitBrief> units = new HashSet<UnitBrief>();
		Set<Long> unitIds = new HashSet<Long>();

		// if current user is AMP Admin, then all units are accessible
		AmpUser user = ampUserService.getCurrentUser();		
		if (roleAssignService.isAdmin()) {
			units = unitRepository.findBy();			
			log.info("The current user " + user.getUsername() + " is Admin and has access to all " + units.size() + " units." );
			return units;
		}
		
		// find all role assignments for current user
		List<RoleAssignmentDetailActions> ras = roleAssignmentRepository.findByUserIdAndUnitIdNotNull(user.getId());
		
		// retrieve the associated units
		for (RoleAssignmentDetailActions ra : ras) {
			UnitBrief unit = ra.getUnit();
			Long unitId = unit.getId();
			if (unitIds.contains(unitId)) continue;			
			unitIds.add(unitId);
			units.add(unit);
		}
		
		log.info("The current user " + user.getUsername() + " has access to " + units.size() + " units." );
		return units;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAccessibleUnits(ActionType, TargetType)
	 */
	@Override
	public ImmutablePair<Set<Long>, Set<UnitBrief>> getAccessibleUnits(ActionType actionType, TargetType targetType) {
		Set<UnitBrief> units = new HashSet<UnitBrief>();
		Set<Long> unitIds = new HashSet<Long>();

		// return null to signal that the current user is AMP Admin, in which case all units are accessible
		AmpUser user = ampUserService.getCurrentUser();		
		if (roleAssignService.isAdmin()) return ImmutablePair.of(null, null);
			
		// find all role assignments for current user
		List<RoleAssignmentDetailActions> ras = roleAssignmentRepository.findByUserIdAndUnitIdNotNull(user.getId());
		
		// retrieve the associated units if the role can perform the action
		for (RoleAssignmentDetailActions ra : ras) {
			UnitBrief unit = ra.getUnit();
			Long unitId = unit.getId();
		
			// skip if the unit of this assignment is already included
			// Note that set of UnitBrief doesn't work directly, as its equals method isn't inherited from Unit, which is based on ID;
			// rather, comparison is based on object ID as is with Object, but object ID of instance retrieved from DB differs on each read
			// there isn't a way to override equals method on UnitBrief as it's an interface
			// the workaround is to use a set of unit IDs to maintain uniqueness
			if (unitIds.contains(unitId)) continue;			
			
			// include the unit of this assignment if the role can perform the action
			for (ActionBrief action : ra.getRole().getActions()) {
				if (action.getActionType() == actionType && action.getTargetType() == targetType) {
					unitIds.add(unitId);
					units.add(unit);
					break;
				}
			}
		}
		
		return ImmutablePair.of(unitIds, units);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAccessibleUnitIds(ActionType, TargetType)
	 */
	@Override
	public Set<Long> getAccessibleUnitIds(ActionType actionType, TargetType targetType) {
		// get accessible units for the action
		Set<Long> unitIds = getAccessibleUnits(actionType, targetType).left;		
		
		// if units is null, i.e. current user is AMP Admin, return null as well to indicate no restraints on unit ID
		if (unitIds == null ) {
			log.info("The current user is admin and can perform action " + actionType + " " + targetType + " in any unit." );
			return null;
		}
			
		// otherwise, if units is empty, throw access denied exception as the user can't perform the action in any unit
		if (unitIds.isEmpty()) {
			throw new AccessDeniedException("The current user cannot perform action " + actionType + " " + targetType  + " in any unit.");
		}
		
		// otherwise return the accessible units IDs
		log.info("The current user can perform action " + actionType + " " + targetType + " in " + unitIds.size() + " units." );
		return unitIds;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.getAcUnitId(Long, Class)
	 */
	public Long getAcUnitId(Long id, Class clazz) {
		// handle WorkflowResult, which is not subtype of Dataentity
		if (clazz == WorkflowResult.class) {
			WorkflowResult result = workflowResultRepository.findById(id).orElseThrow(() -> new StorageException("WorkflowResult <" + id + "> does not exist!"));
			return result.getAcUnitId();
		}
		
		// handle all subclasses of Dataentity		
		Dataentity entity = dataentityService.findDataentity(id, clazz);
		return entity.getAcUnitId();			
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.prefilter(WorkflowResultSearchQuery, ActionType, TargetType)
	 */
	@Override
	public Set<Long> prefilter(WorkflowResultSearchQuery query, ActionType actionType, TargetType targetType) {
		// if action not specified, default to Read WorkflowResult
		if (actionType == null && targetType == null) {
			actionType = ActionType.Read;
			targetType = TargetType.WorkflowResult;
		}
		else if (actionType == null || targetType == null) {
			throw new IllegalArgumentException("The request parameters (actionType, targetType) must be both provided or both null!");			
		}
		
		// get accessible units for Read WorkflowResult, if none, access denied exception will be thrown
		Set<Long> acUnitIds = getAccessibleUnitIds(actionType, targetType);

		// otherwise if acUnitIds is null, i.e. user is admin, then no AC prefilter is needed;  
		if (acUnitIds == null) return acUnitIds;
		
		// otherwise apply AC prefilter by intersecting with user filters		
		Long[] fus = query.getFilterByUnits();
		Set<Long> ftUnitIds = Set.of(fus);
		
		// retain user filtered units if exist, among all accessible units
		if (!ftUnitIds.isEmpty()) {
			// note: ftUnitIds.retainAll(acUnitIds) won't work as ftUnitIds is immutable
			acUnitIds.retainAll(ftUnitIds);
		}

		// if above intersection is empty, the user cannot perform this query
		if (acUnitIds.isEmpty()) {
			throw new AccessDeniedException("The current user cannot query workflow results within the filtered units.");
		}

		// update unit filters in query if changed
		Long[] fusNew = acUnitIds.toArray(fus);	
		
		// note: fusNew.length < fus.length won't work in case fus is empty
		if (fusNew.length != fus.length) {
			query.setFilterByUnits(fusNew);
			log.info("WorkflowResultSearchQuery has been prefiltered with only accessible units.");
		}
		else {
			log.info("WorkflowResultSearchQuery remains the same after AC units prefilter.");				
		}			
		
		return acUnitIds;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.postfilter(WorkflowResultResponse, Set<Long>)
	 */
	@Override
	public void postfilter(WorkflowResultResponse response, Set<Long> acUnitIds) {
		List<WorkflowResultFilterUnit> fus = response.getFilters().getUnits();
		List<WorkflowResultFilterUnit> fusNew = new ArrayList<WorkflowResultFilterUnit>();

		// if acUnitIds is null, i.e. user is admin, then no AC prefilter is needed;  
		if (acUnitIds == null) return;

		// otherwise apply AC postfilter by intersecting with user filters
		for (WorkflowResultFilterUnit fu : fus) {
			if (acUnitIds.contains(fu.getUnitId())) {
				fusNew.add(fu);
			}
		}

		// update unit filters in response if changed
		if (fusNew.size() < fus.size()) {
			response.getFilters().setUnits(fusNew);
			log.info("WorkflowResultResponse has been postfiltered with only accessible units.");
		}
		else {
			log.info("WorkflowResultResponse remains the same after AC units postfilter.");				
		}			
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.prefilter(MgmEvaluationSearchQuery)
	 */
	public Set<Long> prefilter(MgmEvaluationSearchQuery query) {
		// get accessible units for Read MgmEvaluationTest, if none, access denied exception will be thrown
		Set<Long> acUnitIds = getAccessibleUnitIds(ActionType.Read, TargetType.MgmEvaluationTest);

		// otherwise if acUnitIds is null, i.e. user is admin, then no AC prefilter is needed;  
		if (acUnitIds == null) return acUnitIds;
		
		// otherwise apply AC prefilter by intersecting with user filters		
		Long[] fus = query.getFilterByUnits();
		Set<Long> ftUnitIds = Set.of(fus);
		
		// retain user filtered units if exist, among all accessible units
		if (!ftUnitIds.isEmpty()) {
			// note: ftUnitIds.retainAll(acUnitIds) won't work as ftUnitIds is immutable
			acUnitIds.retainAll(ftUnitIds);
		}

		// if above intersection is empty, the user cannot perform this query
		if (acUnitIds.isEmpty()) {
			throw new AccessDeniedException("The current user cannot query MgmEvaluationTest results within the filtered units.");
		}

		// update unit filters in query if changed
		Long[] fusNew = acUnitIds.toArray(fus);	
		
		// note: fusNew.length < fus.length won't work in case fus is empty
		if (fusNew.length != fus.length) {
			query.setFilterByUnits(fusNew);
			log.info("MgmEvaluationSearchQuery has been prefiltered with only accessible units.");
		}
		else {
			log.info("MgmEvaluationSearchQuery remains the same after AC units prefilter.");				
		}			
		
		return acUnitIds;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.PermissionService.postfilter(MgmEvaluationTestResponse, Set<Long>)
	 */
	@Override
	public void postfilter(MgmEvaluationTestResponse response, Set<Long> acUnitIds) {
		List<WorkflowResultFilterUnit> fus = response.getFilters().getUnits();
		List<WorkflowResultFilterUnit> fusNew = new ArrayList<WorkflowResultFilterUnit>();

		// if acUnitIds is null, i.e. user is admin, then no AC prefilter is needed;  
		if (acUnitIds == null) return;

		// otherwise apply AC postfilter by intersecting with user filters
		for (WorkflowResultFilterUnit fu : fus) {
			if (acUnitIds.contains(fu.getUnitId())) {
				fusNew.add(fu);
			}
		}

		// update unit filters in response if changed
		if (fusNew.size() < fus.size()) {
			response.getFilters().setUnits(fusNew);
			log.info("WorkflowResultResponse has been postfiltered with only accessible units.");
		}
		else {
			log.info("WorkflowResultResponse remains the same after AC units postfilter.");				
		}			
	}
	
	
}
