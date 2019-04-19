package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Primary;

@RepositoryRestResource(collectionResourceRel = "primaries", path = "primaries")
public interface PrimaryRepository extends PagingAndSortingRepository<Primary, Long> {

//	List<Primary> findByName(@Param("name") String name);
	
}
