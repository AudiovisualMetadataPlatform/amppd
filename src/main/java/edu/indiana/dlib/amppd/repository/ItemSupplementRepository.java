package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.projection.ItemSupplementBrief;


@RepositoryRestResource(excerptProjection = ItemSupplementBrief.class)
public interface ItemSupplementRepository extends SupplementRepository<ItemSupplement> {

	@RestResource(exported = false)
	int countByItemCollectionUnitId(Long unitId);
	
	@RestResource(exported = false)
	int countByItemCollectionId(Long collectionId);
	
	@RestResource(exported = false)
	int countByItemId(Long itemId);
	
	@RestResource(exported = false)
	List<ItemSupplementBrief> findBy();

	@RestResource(exported = false)
	List<ItemSupplementBrief> findByItemCollectionUnitIdIn(Set<Long> acUnitIds);

	@RestResource(exported = false)
	List<ItemSupplement> findByItemId(Long itemId);

	@RestResource(exported = false)
	List<ItemSupplement> findByItemIdAndName(Long itemId, String name);
	
	@RestResource(exported = false)
	List<ItemSupplement> findByItemIdAndCategory(Long itemId, String category); 
	
	@RestResource(exported = false)
	List<ItemSupplement> findByItemIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long itemId, String category, String format); 
	
	@RestResource(exported = false)
	List<ItemSupplement> findByItemIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long itemId, String name, String category, String format); 

}
