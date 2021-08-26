package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Bundle;


//@RepositoryRestResource(collectionResourceRel = "bundles", path = "bundles")
public interface BundleRepository extends DataentityRepository<Bundle> {
		
	List<Bundle> findByNameAndCreatedBy(String name, String createdBy);
	
	@Query(value = "select b from Bundle b where b.name is not null and b.name != '' and b.primaryfiles.size > 0 order by createdBy, name")
	List<Bundle> findAllWithNonEmptyNameNonEmptyPrimaryfiles();
	
}
