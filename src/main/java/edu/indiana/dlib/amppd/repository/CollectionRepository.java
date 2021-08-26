package edu.indiana.dlib.amppd.repository;

import java.util.List;

import edu.indiana.dlib.amppd.model.Collection;


//@RepositoryRestResource(collectionResourceRel = "collections", path = "collections")
public interface CollectionRepository extends ContentRepository<Collection> {
	
	List<Collection> findByUnitNameAndName(String unitName, String name);

}
