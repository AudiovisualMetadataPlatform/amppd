package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Collection;

@RepositoryRestResource(collectionResourceRel = "collections", path = "collections")
public interface CollectionRepository extends ContentRepository<Collection> {
	
}
