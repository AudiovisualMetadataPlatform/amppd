package edu.iu.dlib.amppd.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.iu.dlib.amppd.model.Supplement;

@RepositoryRestResource(collectionResourceRel = "supplements", path = "supplements")
public interface SupplementRepository extends PagingAndSortingRepository<Supplement, Long> {

//	List<Supplement> findByName(@Param("name") String name);
	
}
