package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.projection.PrimaryfileBrief;
import edu.indiana.dlib.amppd.model.projection.PrimaryfileDeref;


@RepositoryRestResource(excerptProjection = PrimaryfileBrief.class)
public interface PrimaryfileRepository extends AssetRepository<Primaryfile> {
	
	@RestResource(exported = false)
	int countByItemCollectionUnitId(Long unitId);
	
	@RestResource(exported = false)
	int countByItemCollectionId(Long CollectionId);
	
	@RestResource(exported = false)
	int countByItemId(Long itemId);
	
	@RestResource(exported = false)
	List<Primaryfile> findByItemCollectionUnitNameAndItemCollectionNameAndItemNameAndName(String itemCollectionUnitName, String itemCollectionName, String itemName, String name);

	@RestResource(exported = false)
	List<Primaryfile> findByItemIdAndName(Long itemId, String name);
	
	@RestResource(exported = false)
	List<Primaryfile> findByItemCollectionActiveTrueAndHistoryIdNotNull();	
	
	@RestResource(exported = false)
	List<Primaryfile> findByHistoryIdNotNull();	
	
	@RestResource(exported = false)
	List<Primaryfile> findByHistoryId(String historyId); 
	
	@RestResource(exported = false)
	List<Primaryfile> findByMediaInfoNull(); 

	@RestResource(exported = false)
	@Query(value = "select p from Primaryfile p where lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.description) like lower(concat('%', :keyword,'%'))")
	List<Primaryfile> findByKeyword(@Param("keyword") String keyword); 
		
	@RestResource(exported = false)
	@Query(value = "select p from Primaryfile p where (lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%'))) order by p.item.id")
	List<Primaryfile> findByItemOrFileName(@Param("keyword") String keyword);
	
	@RestResource(exported = false)
	@Query(value = "select p from Primaryfile p where (lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%')) or lower(p.item.collection.name) like lower(concat('%', :keyword,'%'))) order by p.item.id")
	List<Primaryfile> findByCollectionOrItemOrFileName(@Param("keyword") String keyword);

	@RestResource(exported = false)
	@Query(nativeQuery = true,
	value = "select p.id, p.name, p.description, " +
			"p.original_filename as originalFilename, p.pathname, p.symlink, cast(p.media_info as varchar) as mediaInfo, " + 
			"p.created_date as createdDate, p.modified_date as modifiedDate, p.created_by as createdBy, p.modified_by as modifiedBy, " +
			"i.id as itemId, i.name as itemName, i.external_id as externalId, i.external_source as externalSource, " + 
			"c.id as collectionId, c.name as collectionName, u.id as unitId, u.name as unitName " +
			"from primaryfile p, item i, collection c, unit u, websearch_to_tsquery(:keyword) q " +
			"where p.item_id = i.id and i.collection_id = c.id and c.unit_id = u.id and c.active = true " +
			"and q @@ to_tsvector(concat(p.name, ' ', p.description, ' ', p.original_filename, ' ', " +
			"i.name, ' ', i.description, ' ', i.external_id, ' ', c.name, ' ', c.description))" +
			"order by i.id, p.name")
	List<PrimaryfileDeref> findActiveByKeyword(String keyword);
	
	@RestResource(exported = false)
	@Query(nativeQuery = true,
	value = "select p.id, p.name, p.description, " +
			"p.original_filename as originalFilename, p.pathname, p.symlink, cast(p.media_info as varchar) as mediaInfo, " + 
			"p.created_date as createdDate, p.modified_date as modifiedDate, p.created_by as createdBy, p.modified_by as modifiedBy, " +
			"i.id as itemId, i.name as itemName, i.external_id as externalId, i.external_source as externalSource, " + 
			"c.id as collectionId, c.name as collectionName, u.id as unitId, u.name as unitName " +
			"from primaryfile p, item i, collection c, unit u, websearch_to_tsquery(:keyword) q " +
			"where p.item_id = i.id and i.collection_id = c.id and c.unit_id = u.id and c.active = true and u.id in :acUnitIds " +
			"and q @@ to_tsvector(concat(p.name, ' ', p.description, ' ', p.original_filename, ' ', " +
			"i.name, ' ', i.description, ' ', i.external_id, ' ', c.name, ' ', c.description))" +
			"order by i.id, p.name")
	List<PrimaryfileDeref> findActiveByKeywordAC(String keyword, Set<Long> acUnitIds);

//	@RestResource(exported = false)
//	@Query(value = "select p from Primaryfile p where p.item.collection.active = true and (lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%')) or lower(p.item.collection.name) like lower(concat('%', :keyword,'%'))) order by p.item.id")
//	List<Primaryfile> findActiveByKeyword(String keyword);
//	
//	@RestResource(exported = false)
//	@Query(value = "select p from Primaryfile p where p.item.collection.active = true and (lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%')) or lower(p.item.collection.name) like lower(concat('%', :keyword,'%'))) and p.item.collection.unit.id in :acUnitIds order by p.item.id")
//	List<Primaryfile> findActiveByKeywordAC(String keyword, Set<Long> acUnitIds);


	/* TODO 
	 *  The API for primaryfile creation can be disabled by setting @RepositoryRestResource export = false for saveOnCreation.
	 *  Currently the saveOnCreation API doesn't require media file when creating a primaryfile; 
	 *  rather, media ingestion can be done with the file upload API after the primaryfile is created;
	 *  this could break integrity of primaryfile data and workflow related operations.
	 *  To achieve both in one step, the saveOnCreation API might need customization (if possible with Spring Data Rest) 
	 *  to allow input Content-Type application/stream, because MultipartFile may not be serializable to JSON.
	 *  Alternatively, primaryfile creation/ingestion can be achieved in one step by the "/items/{itemId}/addPrimaryfile" API.
	 */ 
	
}