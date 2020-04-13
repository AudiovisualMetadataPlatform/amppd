package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "primaryfiles", path = "primaryfiles")
public interface PrimaryfileRepository extends AssetRepository<Primaryfile> {
	@Query(value = "select i from Primaryfile i where lower(i.name) like lower(concat('%', :keyword,'%')) or lower(i.description) like lower(concat('%', :keyword,'%'))")
	List<Primaryfile> findByKeyword(@Param("keyword") String keyword); 
	
}