package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.projection.ItemSupplementBrief;


@RepositoryRestResource(excerptProjection = ItemSupplementBrief.class)
public interface ItemSupplementRepository extends SupplementRepository<ItemSupplement> {
	
	List<ItemSupplementBrief> findBy();
	List<ItemSupplementBrief> findByItemCollectionUnitIdIn(Set<Long> acUnitIds);

	List<ItemSupplement> findByItemId(Long itemId);
	List<ItemSupplement> findByItemIdAndName(Long itemId, String name);
	List<ItemSupplement> findByItemIdAndCategory(Long itemId, String category); 
	List<ItemSupplement> findByItemIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long itemId, String category, String format); 
	List<ItemSupplement> findByItemIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long itemId, String name, String category, String format); 

}
