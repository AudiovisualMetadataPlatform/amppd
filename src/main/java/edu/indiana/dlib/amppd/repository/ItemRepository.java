package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.projection.ItemBrief;
import edu.indiana.dlib.amppd.model.projection.ItemDeref;


@RepositoryRestResource(excerptProjection = ItemBrief.class)
public interface ItemRepository extends ContentRepository<Item> {
	
	@RestResource(exported = false)
	int countByCollectionUnitId(Long unitId);
	
	@RestResource(exported = false)
	int countByCollectionId(Long CollectionId);
	
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
	
//	@Deprecated
//	@RestResource(exported = false)
//	@Query(value = "select i from Item i where lower(i.name) like lower(concat('%', :keyword,'%')) or lower(i.description) like lower(concat('%', :keyword,'%'))")
//	List<ItemBrief> findByKeyword(@Param("keyword") String keyword);		
//
//	@Deprecated
//	@RestResource(exported = false)
//	@Query(value = "select i from Item i where (lower(i.name) like lower(concat('%', :keyword,'%')) or lower(i.description) like lower(concat('%', :keyword,'%'))) and i.collection.unit.id in :acUnitIds")
//	List<ItemBrief> findByKeywordAC(@Param("keyword") String keyword, Set<Long> acUnitIds);		
	
	// full text search on item using native PostgreSql search tools, without AC	
	@RestResource(exported = false)
	@Query(nativeQuery = true,
	value = "select i.id, i.name, i.description, i.external_id as externalId, i.external_source as externalSource," + 
			" i.created_date as createdDate, i.modified_date as modifiedDate, i.created_by as createdBy, i.modified_by as modifiedBy," +
			" c.name as collectionName, u.name as unitName, c.id as collectionId, u.id as unitId" + 
			" from item i, collection c, unit u, websearch_to_tsquery(:keyword) q" +
			" where i.collection_id = c.id and c.unit_id = u.id" +
			" and q @@ to_tsvector(concat(i.name, ' ', i.description, ' ', i.external_id))" + 
			" order by i.name")
	List<ItemDeref> findByKeyword(String keyword);
	
	// full text search on item using native PostgreSql search tools, , with AC
	@RestResource(exported = false)
	@Query(nativeQuery = true,
	value = "select i.id, i.name, i.description, i.external_id as externalId, i.external_source as externalSource," + 
			" i.created_date as createdDate, i.modified_date as modifiedDate, i.created_by as createdBy, i.modified_by as modifiedBy," +
			" c.name as collectionName, u.name as unitName, c.id as collectionId, u.id as unitId" + 
			" from item i, collection c, unit u, websearch_to_tsquery(:keyword) q" +
			" where i.collection_id = c.id and c.unit_id = u.id and u.id in :acUnitIds" +
			" and q @@ to_tsvector(concat(i.name, ' ', i.description, ' ', i.external_id))" + 
			" order by i.name")
	List<ItemDeref> findByKeywordAC(String keyword, Set<Long> acUnitIds);		
		
	@RestResource(exported = false)
	@Modifying
	@Query(value = "update Item set name = :name where id = :id") 
	int updateName(@Param("name") String name, @Param("id") Long id);
	 
}
