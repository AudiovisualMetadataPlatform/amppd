package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;

import edu.indiana.dlib.amppd.model.Dataentity;


@NoRepositoryBean
public interface DataentityRepository<S extends Dataentity> extends AmpObjectRepository<S> {
	
	List<S> findByName(String name);
	List<S> findByDescription(String description); 
	
	List<S> findByNameContainingIgnoreCase(String keyword);
	List<S> findByDescriptionContainingIgnoreCase(String keyword); 
	List<S> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String keywordName, String keywordDescription);

}
