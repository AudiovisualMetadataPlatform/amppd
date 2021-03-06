package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.ItemSupplement;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "itemSupplements", path = "itemSupplements")
public interface ItemSupplementRepository extends SupplementRepository<ItemSupplement> {
	
	List<ItemSupplement> findByItemIdAndName(Long itemId, String name);

}
