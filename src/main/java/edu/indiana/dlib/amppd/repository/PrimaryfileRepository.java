package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.projection.PrimaryfileBrief;


@RepositoryRestResource(excerptProjection = PrimaryfileBrief.class)
public interface PrimaryfileRepository extends AssetRepository<Primaryfile> {
	
	List<Primaryfile> findByItemCollectionUnitNameAndItemCollectionNameAndItemNameAndName(String itemCollectionUnitName, String itemCollectionName, String itemName, String name);
	List<Primaryfile> findByItemIdAndName(Long itemId, String name);
	
	List<Primaryfile> findByItemCollectionActiveTrueAndHistoryIdNotNull();	
	List<Primaryfile> findByHistoryIdNotNull();	
	List<Primaryfile> findByHistoryId(String historyId); 
	
	List<Primaryfile> findByMediaInfoNull(); 

	@Query(value = "select p from Primaryfile p where lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.description) like lower(concat('%', :keyword,'%'))")
	List<Primaryfile> findByKeyword(@Param("keyword") String keyword); 
		
	@Query(value = "select p from Primaryfile p where (lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%'))) order by p.item.id")
	List<Primaryfile> findByItemOrFileName(@Param("keyword") String keyword);
	
	@Query(value = "select p from Primaryfile p where (lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%')) or lower(p.item.collection.name) like lower(concat('%', :keyword,'%'))) order by p.item.id")
	List<Primaryfile> findByCollectionOrItemOrFileName(@Param("keyword") String keyword);

	@Query(value = "select p from Primaryfile p where p.item.collection.active = true and (lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%')) or lower(p.item.collection.name) like lower(concat('%', :keyword,'%'))) order by p.item.id")
	List<Primaryfile> findActiveByKeyword(String keyword);
	@Query(value = "select p from Primaryfile p where p.item.collection.active = true and (lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%')) or lower(p.item.collection.name) like lower(concat('%', :keyword,'%'))) and p.item.collection.unit.id in :acUnitIds order by p.item.id")
	List<Primaryfile> findActiveByKeywordAC(String keyword, Set<Long> acUnitIds);


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