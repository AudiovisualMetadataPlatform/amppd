package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.projection.CollectionBrief;


//@RepositoryRestResource(collectionResourceRel = "collections", path = "collections")
@RepositoryRestResource(excerptProjection = CollectionBrief.class)
public interface CollectionRepository extends ContentRepository<Collection> {
	
	List<Collection> findByUnitNameAndName(String unitName, String name);
	List<Collection> findByUnitIdAndName(Long unitId, String name);

}
