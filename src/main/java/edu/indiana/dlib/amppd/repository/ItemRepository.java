package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.projection.ItemBrief;


@RepositoryRestResource(excerptProjection = ItemBrief.class)
public interface ItemRepository extends ContentRepository<Item> {
	
//	@Override
//	@RestResource(exported = false)
//	List<Item> findAll();

	@RestResource(exported = false)
	Item findFirstByExternalSourceAndExternalId(String externalSource, String externalId);
	
	@RestResource(exported = false)
	List<Item> findByCollectionUnitNameAndCollectionNameAndName(String collectionUnitName, String collectionName, String name);

	@RestResource(exported = false)
	List<Item> findByCollectionIdAndName(Long collectionId, String name);

	@RestResource(exported = false)
	List<Item> findByCollectionIdAndExternalSourceAndExternalId(Long collectionId, String externalSource, String externalId);

	// TODO tried various ways below to achieve case-insensitive keyword match, but none worked
	//	// Note: ilike is a PostgreSQL extension, not part of standard SQL, so need to use nativeQuery. However, the query still fails with exception.
	//	@Query(value = "select i from Item i where i.name ilike %:keyword% or i.description ilike %:keyword%", nativeQuery = true)
	//	@Query(value = "select i from Item i where lower(i.name) like %:keyword.toLowerCase()% or lower(i.description) like %:keyword.toLowerCase()%")
	//	@Query(value = "select i from Item i where lower(i.name) like %:#{keyword.toLowerCase()}% or lower(i.description) like %:#{keyword.toLowerCase()}%")
	
	@RestResource(exported = false)
	@Query(value = "select i from Item i where lower(i.name) like lower(concat('%', :keyword,'%')) or lower(i.description) like lower(concat('%', :keyword,'%'))")
	List<ItemBrief> findByKeyword(@Param("keyword") String keyword);		

	@RestResource(exported = false)
	@Query(value = "select i from Item i where (lower(i.name) like lower(concat('%', :keyword,'%')) or lower(i.description) like lower(concat('%', :keyword,'%'))) and i.collection.unit.id in :acUnitIds")
	List<ItemBrief> findByKeywordAC(@Param("keyword") String keyword, Set<Long> acUnitIds);		
	
	@RestResource(exported = false)
	@Modifying
	@Query(value = "update Item set name = :name where id = :id") 
	int updateName(@Param("name") String name, @Param("id") Long id);
	 
}
