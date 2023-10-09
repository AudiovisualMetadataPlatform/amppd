package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.CrudRepository;

import edu.indiana.dlib.amppd.model.BatchFile;


public interface BatchFileRepository extends CrudRepository<BatchFile, Long>{
	
}
