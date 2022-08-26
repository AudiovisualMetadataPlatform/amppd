package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;

import edu.indiana.dlib.amppd.model.MgmMeta;

@NoRepositoryBean
public interface MgmMetaRepository<S extends MgmMeta> extends AmpObjectRepository<S>  {

	List<S> findByNameContainingIgnoreCase(String keyword);
	List<S> findByDescriptionContainingIgnoreCase(String keyword); 
	List<S> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String keywordName, String keywordDescription);

}
