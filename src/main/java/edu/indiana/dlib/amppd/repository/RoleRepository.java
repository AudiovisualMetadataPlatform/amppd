package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.ac.Role;
import edu.indiana.dlib.amppd.model.projection.RoleBrief;
import edu.indiana.dlib.amppd.model.projection.RoleBriefActions;

//@RepositoryRestResource(excerptProjection = RoleBrief.class)
@RepositoryRestResource(exported = false)
public interface RoleRepository extends AmpObjectRepository<Role> {

	Role findFirstByNameAndUnitId(String name, Long unitId);
	Role findFirstByNameAndUnitIdIsNull(String name);

	// for role_action config
	List<RoleBriefActions> findByUnitIdIsNullOrderByLevel();					// global roles
	List<RoleBriefActions> findByUnitIdOrderByLevel(Long unitId);				// unit roles
	List<RoleBriefActions> findByUnitIdIsNullOrUnitIdOrderByLevel(Long unitId);	// global or unit roles
		
	// viewable roles: global or unit roles excluding AMP Admin
//	@Query(value = "select new RoleBrief(r.id as id, r.unit.id as unitId, r.name as name, r.level as level) from Role r where r.name <> 'AMP Admin' and (r.unit is null or r.unit.id = :unitId) order by r.level")
	@Query(value = "select r from Role r where r.name <> 'AMP Admin' and (r.unit is null or r.unit.id = :unitId) order by r.level")
	List<RoleBrief> findViewableRolesInUnit(Long unitId);					

	// assignable roles: global or unit roles excluding AMP Admin, with level greater than the given threshold
//	@Query(value = "select new RoleBrief(r.id as id, r.unit.id as unitId, r.name as name, r.level as level) from Role r where r.name <> 'AMP Admin' and r.level > :level and (r.unit is null or r.unit.id = :unitId) order by r.level")
	@Query(value = "select r from Role r where r.name <> 'AMP Admin' and r.level > :level and (r.unit is null or r.unit.id = :unitId) order by r.level")
	List<RoleBrief> findAssignableRolesInUnit(Integer level, Long unitId);
	
	// delete unrefreshed global roles
	List<Role> deleteByUnitIdIsNullAndModifiedDateBefore(Date date);
	
}
