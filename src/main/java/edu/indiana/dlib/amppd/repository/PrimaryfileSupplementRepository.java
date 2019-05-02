package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;

@RepositoryRestResource(collectionResourceRel = "primaryfileSupplements", path = "primaryfileSupplements")
public interface PrimaryfileSupplementRepository extends SupplementRepository<PrimaryfileSupplement> {
	
}
