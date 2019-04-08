package edu.iu.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.iu.dlib.amppd.model.Group;

@RepositoryRestResource(collectionResourceRel = "groups", path = "groups")
public interface GroupRepository extends PagingAndSortingRepository<Group, Long> {

	List<Group> findByName(@Param("name") String name);
	
}
