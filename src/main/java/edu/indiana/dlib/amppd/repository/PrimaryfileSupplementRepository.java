package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "primaryfileSupplements", path = "primaryfileSupplements")
public interface PrimaryfileSupplementRepository extends SupplementRepository {
	
}
