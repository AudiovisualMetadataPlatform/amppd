package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Bundle;

@RepositoryRestResource(collectionResourceRel = "bundles", path = "bundles")
public interface BundleRepository extends PagingAndSortingRepository<Bundle, Long> {

	List<Bundle> findByName(@Param("name") String name);
	
}
