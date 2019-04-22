package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Unit;

@RepositoryRestResource(collectionResourceRel = "units", path = "units")
public interface UnitRepository extends PagingAndSortingRepository<Unit, Long> {

	List<Unit> findByName(@Param("name") String name);
	
}
