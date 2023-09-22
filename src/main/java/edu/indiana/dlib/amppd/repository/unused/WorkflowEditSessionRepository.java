package edu.indiana.dlib.amppd.repository.unused;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.TimedToken;

@RepositoryRestResource(exported = false)
public interface WorkflowEditSessionRepository extends CrudRepository<TimedToken, Long> {	
	// TODO This class currently not in use, move to unused package
}
