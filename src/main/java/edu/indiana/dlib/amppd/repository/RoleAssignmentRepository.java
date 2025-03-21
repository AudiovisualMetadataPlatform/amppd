package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;

import edu.indiana.dlib.amppd.model.ac.RoleAssignment;
import edu.indiana.dlib.amppd.model.projection.RoleAssignmentBrief;
import edu.indiana.dlib.amppd.model.projection.RoleAssignmentDetail;
import edu.indiana.dlib.amppd.model.projection.RoleAssignmentDetailActions;


public interface RoleAssignmentRepository extends AmpObjectRepository<RoleAssignment> {

	boolean existsByUserIdAndRoleIdIn(Long userId, List<Long> roleIds);
	boolean existsByUserIdAndRoleIdAndUnitId(Long userId, Long roleId, Long unitId);		
	boolean existsByUserIdAndRoleIdInAndUnitId(Long userId, List<Long> roleIds, Long unitId);
	boolean existsByUserIdAndRoleNameAndUnitIdIsNull(Long userId, String roleName);
	boolean existsByUserIdAndRoleIdAndUnitIdIsNull(Long userId, Long roleId);
	boolean existsByUserIdAndRoleIdInAndUnitIdIsNull(Long userId, List<Long> roleIds);	
	
	RoleAssignment findFirstByUserIdAndRoleIdAndUnitId(Long userId, Long roleId, Long unitId);	
	RoleAssignment findFirstByUserIdAndRoleIdAndUnitIdNull(Long userId, Long roleId);	
	
	List<RoleAssignmentBrief> findByUserId(Long userId);	
	List<RoleAssignmentBrief> findByUserIdAndRoleIdInAndUnitId(Long userId, List<Long> roleIds, Long unitId);

	// for user's permitted actions
	List<RoleAssignmentDetailActions> findByUserIdOrderByUnitId(Long userId);	
	List<RoleAssignmentDetailActions> findByUserIdAndUnitIdInOrderByUnitId(Long userId, List<Long> unitIds);	

	// for user accessible units
	List<RoleAssignmentDetailActions> findByUserIdAndUnitIdNotNull(Long userId);

	// for user role assignment
	List<RoleAssignmentDetail> findByUnitIdOrderByUserId(Long unitId);

	// find the lowest role level for the user in a unit
	@Query(value = "select min(ra.role.level) from RoleAssignment ra where ra.user.id = :userId and (ra.unit is null or ra.unit.id = :unitId)")
	Integer findMinRoleLevelByUserIdAndUnitId(Long userId, Long unitId);
	
	// un-assign user role
	RoleAssignmentBrief deleteByUserIdAndRoleIdAndUnitId(Long userId, Long roleId, Long unitId);
	
	// delete RoleAssignments within a unit
	List<RoleAssignmentBrief> deleteByUnitId(Long unitId);

}
