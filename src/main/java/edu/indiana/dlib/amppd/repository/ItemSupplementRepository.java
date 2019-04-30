package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "itemSupplements", path = "itemSupplements")
public interface ItemSupplementRepository extends SupplementRepository {
	
}
