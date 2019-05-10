package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Item;

@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface ItemRepository extends PagingAndSortingRepository<Item, Long> {

	List<Item> findByName(@Param("name") String name);
	
	/*
	 * List<Item> findByDescription(@Param("description") String description);
	 * 
	 * List<Item> findByCreatedDate(@Param("createdDate") String createdDate);
	 * 
	 * List<Item> findByCreatedBy(@Param("createdBy") String createdBy);
	 */
	
	
	
}
