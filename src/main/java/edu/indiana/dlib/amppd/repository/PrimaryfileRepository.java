package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;

@RepositoryRestResource(collectionResourceRel = "primaryfiles", path = "primaryfiles")
public interface PrimaryfileRepository extends AssetRepository<Primaryfile> {
	
	List<Primaryfile> findByDescription(@Param("description") String description);
	
	List<Primaryfile> findByCreatedDate(@Param("createdDate") String createdDate);
	
	List<Primaryfile> findByCreatedBy(@Param("createdBy") String createdBy);
	
}
