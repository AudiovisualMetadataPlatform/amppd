package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.ac.Role;

@RepositoryRestResource()
public interface RoleRepository extends AmpObjectRepository<Role> {

	Role findFirstByNameAndUnitId(String name, Long unitId);
	Role findFirstByNameAndUnitIdIsNull(String name);

	List<Role> findByUnitIdIsNullOrderByLevelAsc();				// global roles
	List<Role> findByUnitIdOrderByLevelAsc(Long unitId);			// unit roles
	List<Role> findByUnitIdIsNullOrIsOrderByLevel(Long unitId);	// global or unit roles

	@Query(value = "select r from Role r where r.level > :level and (r.unitId = :unitId or r.unitId is null) order by r.level")
	List<Role> findLevelGreaterAndUnitIdIsOrNull(Integer level, Long unitId);
	
}
