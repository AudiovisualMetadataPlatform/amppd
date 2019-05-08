package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Bundle;

@RepositoryRestResource(collectionResourceRel = "bundles", path = "bundles")
public interface BundleRepository extends DataentityRepository<Bundle> {
	
}
