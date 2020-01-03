package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.ItemSupplement;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "itemSupplements", path = "itemSupplements")
public interface ItemSupplementRepository extends SupplementRepository<ItemSupplement> {
}
