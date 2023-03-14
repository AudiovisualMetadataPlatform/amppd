package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.dto.RoleDto;
import edu.indiana.dlib.amppd.model.projection.RoleBrief;

@RepositoryRestResource(excerptProjection = RoleBrief.class)
public interface RoleRepository extends AmpObjectRepository<Role> {

	Role findFirstByNameAndUnitId(String name, Long unitId);
	Role findFirstByNameAndUnitIdIsNull(String name);

	List<Role> findByUnitIdIsNullOrderByLevelAsc();					// global roles
	List<Role> findByUnitIdOrderByLevelAsc(Long unitId);			// unit roles
	List<Role> findByUnitIdIsNullOrUnitIdOrderByLevel(Long unitId);	// global or unit roles
		
	// viewable roles: global or unit roles excluding AMP Admin
	@Query(value = "select r.id, r.uit.id, r.name, r.level from Role r where r.name <> 'AMP Admin' and (r.unit is null or r.unit.id = :unitId) order by r.level")
	List<RoleDto> findViewableRolesInUnit(Long unitId);					

	// assignable roles: global or unit roles excluding AMP Admin, with level greater than current user's min role level
	@Query(value = "select r.id, r.uit.id, r.name, r.level from Role r where r.name <> 'AMP Admin' and r.level > :level and (r.unit is null or r.unit.id = :unitId) order by r.level")
	List<RoleDto> findAssignableRolesInUnit(Integer level, Long unitId);
	
}
