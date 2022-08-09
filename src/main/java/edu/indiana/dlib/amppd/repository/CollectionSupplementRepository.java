package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.projection.CollectionSupplementBrief;


@RepositoryRestResource(excerptProjection = CollectionSupplementBrief.class)
public interface CollectionSupplementRepository extends SupplementRepository<CollectionSupplement> {

	List<CollectionSupplement> findByCollectionId(Long collectionId);
	List<CollectionSupplement> findByCollectionIdAndName(Long collectionId, String name);
	List<CollectionSupplement> findByCollectionIdAndCategory(Long collectionId, String category); 
	List<CollectionSupplement> findByCollectionIdAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long collectionId, String category, String format); 
	List<CollectionSupplement> findByCollectionIdAndNameAndCategoryAndOriginalFilenameEndsWithIgnoreCase(Long collectionId, String name, String category, String format); 

}
