package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.BatchFile;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "batchSupplementFile", path = "batchSupplementFile")
public interface BatchFileRepository extends CrudRepository<BatchFile, Long>{
	
}
