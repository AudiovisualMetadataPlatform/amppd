package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Primaryfile;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "collections", path = "collections")
public interface CollectionRepository extends ContentRepository<Collection> {
	
	@Query(value = "select c from Collection c where c.unit.name = :unitName and c.name = :name")
	List<Collection> findByUnitNameAndName(String unitName, String name);

}
