package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement;

//@NoRepositoryBean
@RepositoryRestResource(collectionResourceRel = "supplements", path = "supplements")
public interface SupplementRepository extends PagingAndSortingRepository<Supplement, Long> {
	List<Primaryfile> findByName(@Param("name") String name);

	List<Supplement> findByOriginalFilename(@Param("originalFilename") String originalFilename);
	
}
