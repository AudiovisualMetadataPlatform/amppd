package edu.indiana.dlib.amppd.repository;

import java.util.List;

import edu.indiana.dlib.amppd.model.ItemSupplement;


//@RepositoryRestResource(collectionResourceRel = "itemSupplements", path = "itemSupplements")
public interface ItemSupplementRepository extends SupplementRepository<ItemSupplement> {
	
	List<ItemSupplement> findByItemIdAndName(Long itemId, String name);
	List<ItemSupplement> findByItemIdAndCategory(Long itemId, String category); 
	List<ItemSupplement> findByItemIdAndCategoryAndOriginalFilenameEndsWith(Long itemId, String category, String format); 
	List<ItemSupplement> findByItemIdAndNameAndCategoryAndOriginalFilenameEndsWith(Long itemId, String name, String category, String format); 

}
