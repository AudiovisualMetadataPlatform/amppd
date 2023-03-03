package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.ac.RoleAssignment;


@RepositoryRestResource()
public interface RoleAssignmentRepository extends AmpObjectRepository<RoleAssignment> {

	boolean existsByUserIdAndRoleIdAndUnitId(Long userId, Long roleId, Long unitId);		
	boolean existsByUserIdAndRoleIdInAndUnitId(Long userId, List<Long> roleIds, Long unitId);
	boolean existsByUserIdAndRoleIdAndUnitIdIsNull(Long userId, Long roleId);
	boolean existsByUserIdAndRoleIdInAndUnitIdIsNull(Long userId, List<Long> roleIds);	
	
	RoleAssignment findFirstByUserIdAndRoleIdAndUnitId(Long userId, Long roleId, Long unitId);	

	List<RoleAssignment> findByUserId(Long userId);	
	List<RoleAssignment> findByUserIdAndRoleIdInAndUnitId(Long userId, List<Long> roleIds, Long unitId);

	List<RoleAssignment> findByUserIdOrderByUnitId(Long userId);	
	List<RoleAssignment> findByUserIdAndUnitIdInOrderByUnitId(Long userId, List<Long> unitIds);	

}
