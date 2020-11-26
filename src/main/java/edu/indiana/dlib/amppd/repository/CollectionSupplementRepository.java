package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Supplement;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "collectionSupplements", path = "collectionSupplements")
public interface CollectionSupplementRepository extends SupplementRepository<CollectionSupplement> {

	List<CollectionSupplement> findByCollectionIdAndName(Long collectionId, String name);

}
