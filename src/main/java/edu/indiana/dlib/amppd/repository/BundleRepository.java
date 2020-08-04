package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.Bundle;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "bundles", path = "bundles")
public interface BundleRepository extends DataentityRepository<Bundle> {
		
	List<Bundle> findByNameAndCreatedBy(String name, String createdBy);
	
	@Query(value = "select b from Bundle b where b.name is not null and b.name != '' and b.primaryfiles.size > 0")
	List<Bundle> findAllWithNonEmptyNameNonEmptyPrimaryfiles();
	
}
