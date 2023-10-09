package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.CrudRepository;

import edu.indiana.dlib.amppd.model.Batch;


public interface BatchRepository extends CrudRepository<Batch, Long>{
	
}
