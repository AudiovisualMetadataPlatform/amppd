package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Primaryfile;

@RepositoryRestResource(collectionResourceRel = "primaryfiles", path = "primaryfiles")
public interface PrimaryfileRepository extends PagingAndSortingRepository<Primaryfile, Long> {

	List<Primaryfile> findByName(@Param("name") String name);
	
	List<Primaryfile> findByOriginalFilename(@Param("originalFilename") String originalFilename);
	
}
