package edu.indiana.dlib.amppd.repository;

import java.util.List;

import edu.indiana.dlib.amppd.model.ItemSupplement;


//@RepositoryRestResource(collectionResourceRel = "itemSupplements", path = "itemSupplements")
public interface ItemSupplementRepository extends SupplementRepository<ItemSupplement> {
	
	List<ItemSupplement> findByItemIdAndName(Long itemId, String name);

}
