package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.projection.CollectionSupplementBrief;


@RepositoryRestResource(excerptProjection = CollectionSupplementBrief.class)
public interface CollectionSupplementRepository extends SupplementRepository<CollectionSupplement> {

//	@Override
//	@RestResource(exported = false)
//	List<CollectionSupplement> findAll();

	@RestResource(exported = false)
	List<CollectionSupplementBrief> findBy();

	@RestResource(exported = false)
	List<CollectionSupplementBrief> findByCollectionUnitIdIn(Set<Long> acUnitIds);

	@RestResource(exported = false)
	List<CollectionSupplement> findByCollectionId(Long collectionId);

	@RestResource(exported = false)
	List<CollectionSupplement> findByCollectionIdAndName(Long collectionId, String name);

	@RestResource(exported = false)
	List<CollectionSupplement> findByCollectionIdAndCategory(Long collectionId, String category); 

	@RestResource(exported = false)
	List<CollectionSupplement> findByCollectionIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long collectionId, String category, String format); 

	@RestResource(exported = false)
	List<CollectionSupplement> findByCollectionIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long collectionId, String name, String category, String format); 

}
