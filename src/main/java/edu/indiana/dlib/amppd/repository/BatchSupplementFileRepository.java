package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.BatchSupplementFile;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "batchSupplement", path = "batchSupplement")
public interface BatchSupplementFileRepository extends CrudRepository<BatchSupplementFile, Long>{
	
}
