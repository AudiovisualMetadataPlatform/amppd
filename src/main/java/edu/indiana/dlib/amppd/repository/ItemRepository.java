package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Item;

@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface ItemRepository extends ContentRepository<Item> {
	
	@Query("select i from Item i where i.name like %:keyword% or i.description like %:keyword%")
	List<Item> findByKeyword(@Param("keyword") String keyword); 

}
