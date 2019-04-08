package edu.iu.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.iu.dlib.amppd.model.Collection;
import edu.iu.dlib.amppd.model.Unit;

@RepositoryRestResource(collectionResourceRel = "units", path = "units")
public interface UnitRepository extends PagingAndSortingRepository<Unit, Long> {

	List<Collection> findByName(@Param("name") String name);
	
}
