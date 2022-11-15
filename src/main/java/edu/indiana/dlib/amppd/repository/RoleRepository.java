package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.ac.Role;

@RepositoryRestResource()
public interface RoleRepository extends AmpObjectRepository<Role> {

	Role findFirstByNameAndUnitId(String name, Long unitId);
	List<Role> findByNameAndUnitId(String name, Long unitId);

}
