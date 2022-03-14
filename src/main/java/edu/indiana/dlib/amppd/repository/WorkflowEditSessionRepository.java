package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.TimedToken;

@RepositoryRestResource(exported = false)
public interface WorkflowEditSessionRepository extends CrudRepository<TimedToken, Long> {	

}
