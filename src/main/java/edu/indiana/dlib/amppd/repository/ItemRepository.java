package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Item;

@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface ItemRepository extends ContentRepository<Item> {
	
	List<Item> findByDescription(@Param("description") String description);
	
	List<Item> findByCreatedDate(@Param("createdDate") String createdDate);
	
	List<Item> findByCreatedBy(@Param("createdBy") String createdBy);
	
	
	
}
