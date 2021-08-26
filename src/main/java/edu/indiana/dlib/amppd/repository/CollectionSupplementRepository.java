package edu.indiana.dlib.amppd.repository;

import java.util.List;

import edu.indiana.dlib.amppd.model.CollectionSupplement;


//@RepositoryRestResource(collectionResourceRel = "collectionSupplements", path = "collectionSupplements")
public interface CollectionSupplementRepository extends SupplementRepository<CollectionSupplement> {

	List<CollectionSupplement> findByCollectionIdAndName(Long collectionId, String name);

}
