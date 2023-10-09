package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.projection.CollectionBrief;


@RepositoryRestResource(excerptProjection = CollectionBrief.class)
public interface CollectionRepository extends ContentRepository<Collection> {

//	@Override
//	@RestResource(exported = false)
//	List<Collection> findAll();

	@RestResource(exported = false)
	Collection findFirstByUnitNameAndName(String unitName, String name);
	
	@RestResource(exported = false)
	List<Collection> findByUnitNameAndName(String unitName, String name);
	
	@RestResource(exported = false)
	List<Collection> findByUnitIdAndName(Long unitId, String name);

}
