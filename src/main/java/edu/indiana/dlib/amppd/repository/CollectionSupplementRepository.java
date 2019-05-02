package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.CollectionSupplement;

@RepositoryRestResource(collectionResourceRel = "collectionSupplements", path = "collectionSupplements")
public interface CollectionSupplementRepository extends SupplementRepository<CollectionSupplement> {
	
}
