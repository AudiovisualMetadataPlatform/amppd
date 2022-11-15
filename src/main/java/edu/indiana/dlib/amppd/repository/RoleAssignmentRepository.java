package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.ac.RoleAssignment;


@RepositoryRestResource()
public interface RoleAssignmentRepository extends AmpObjectRepository<RoleAssignment> {

	boolean existsByUserIdAndRoleIdInAndUnitId(Long userId, List<Long> roleIds, Long unitId);
	boolean existsByUserIdAndRoleIdInAndUnitIdIsNull(Long userId, List<Long> roleIds);	
	
	RoleAssignment findFirstByUserIdAndRoleIdInAndUnitId(Long userId, List<Long> roleIds, Long unitId);	
	List<RoleAssignment> findByUserIdAndRoleIdInAndUnitId(Long userId, List<Long> roleIds, Long unitId);
	
	boolean existsByUserIdAndRoleIdAndUnitId(Long userId, Long roleId, Long unitId);		
	RoleAssignment findFirstByUserIdAndRoleIdAndUnitId(Long userId, Long roleId, Long unitId);	
	List<RoleAssignment> findByUserIdAndRoleIdAndUnitId(Long userId, Long roleId, Long unitId);
	
	
}
