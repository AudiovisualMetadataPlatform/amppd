package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "collectionSupplements", path = "collectionSupplements")
public interface CollectionSupplementRepository extends SupplementRepository {
	
}
