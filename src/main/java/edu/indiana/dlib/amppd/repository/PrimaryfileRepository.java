package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.Primaryfile;


//@RepositoryRestResource(collectionResourceRel = "primaryfiles", path = "primaryfiles")
public interface PrimaryfileRepository extends AssetRepository<Primaryfile> {
	
	List<Primaryfile> findByItemCollectionUnitNameAndItemCollectionNameAndItemNameAndName(String itemCollectionUnitName, String itemCollectionName, String itemName, String name);
	List<Primaryfile> findByItemIdAndName(Long itemId, String name);
	
	List<Primaryfile> findByHistoryIdNotNull();	
	List<Primaryfile> findByHistoryId(String historyId); 

	@Query(value = "select p from Primaryfile p where lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.description) like lower(concat('%', :keyword,'%'))")
	List<Primaryfile> findByKeyword(@Param("keyword") String keyword); 
		
	@Query(value = "select p from Primaryfile p where ( lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%'))) order by p.item.id")
	List<Primaryfile> findByItemOrFileName(@Param("keyword") String keyword);
	
	@Query(value = "select p from Primaryfile p where ( lower(p.name) like lower(concat('%', :keyword,'%')) or lower(p.item.name) like lower(concat('%', :keyword,'%')) or lower(p.item.collection.name) like lower(concat('%', :keyword,'%'))) order by p.item.id")
	List<Primaryfile> findByCollectionOrItemOrFileName(@Param("keyword") String keyword);

}