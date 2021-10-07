package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.BatchFile;


@RepositoryRestResource(exported = false)
public interface BatchFileRepository extends CrudRepository<BatchFile, Long>{
	
}
