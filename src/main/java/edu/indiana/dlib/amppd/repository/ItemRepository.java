package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.Item;


@CrossOrigin(origins = "*")
//@RepositoryRestResource(excerptProjection = DataentityBrief.class)
//@RepositoryRestResource(collectionResourceRel = "items", path = "items")
public interface ItemRepository extends ContentRepository<Item> {
	
	List<Item> findByCollectionUnitNameAndCollectionNameAndName(String collectionUnitName, String collectionName, String name);
	
	// TODO tried various ways below to achieve case-insensitive keyword match, but none worked
	//	// Note: ilike is a PostgreSQL extension, not part of standard SQL, so need to use nativeQuery. However, the query still fails with exception.
	//	@Query(value = "select i from Item i where i.name ilike %:keyword% or i.description ilike %:keyword%", nativeQuery = true)
	//	@Query(value = "select i from Item i where lower(i.name) like %:keyword.toLowerCase()% or lower(i.description) like %:keyword.toLowerCase()%")
	//	@Query(value = "select i from Item i where lower(i.name) like %:#{keyword.toLowerCase()}% or lower(i.description) like %:#{keyword.toLowerCase()}%")
	
	@Query(value = "select i from Item i where lower(i.name) like lower(concat('%', :keyword,'%')) or lower(i.description) like lower(concat('%', :keyword,'%'))")
	List<Item> findByKeyword(@Param("keyword") String keyword);		
	
	@Transactional
	@Modifying
	@Query(value = "update Item set name = :name where id = :id") 
	int updateName(@Param("name") String name, @Param("id") Long id);
	 
}
