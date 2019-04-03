package edu.iu.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.iu.dlib.amppd.model.Collection;

@RepositoryRestResource(collectionResourceRel = "collections", path = "collections")
public interface CollectionRepository extends PagingAndSortingRepository<Collection, Long> {

	List<Collection> findByName(@Param("name") String name);
	
}
