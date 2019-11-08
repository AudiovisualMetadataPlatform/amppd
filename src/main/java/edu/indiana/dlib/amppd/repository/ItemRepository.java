package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.Item;


@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface ItemRepository extends ContentRepository<Item> {
	
	// TODO tried various ways below to achieve case-insensitive keyword match, but none worked
//	// Note: ilike is a PostgreSQL extension, not part of standard SQL, so need to use nativeQuery. However, the query still fails with exception.
//	@Query(value = "select i from Item i where i.name ilike %:keyword% or i.description ilike %:keyword%", nativeQuery = true)
//	@Query(value = "select i from Item i where lower(i.name) like %:keyword.toLowerCase()% or lower(i.description) like %:keyword.toLowerCase()%")
//	@Query(value = "select i from Item i where lower(i.name) like %:#{keyword.toLowerCase()}% or lower(i.description) like %:#{keyword.toLowerCase()}%")
	
	@Query(value = "select i from Item i where i.name like %:keyword% or i.description like %:keyword%")
	List<Item> findByKeyword(@Param("keyword") String keyword); 

}
