package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.NoRepositoryBean;

import edu.indiana.dlib.amppd.model.Content;

@NoRepositoryBean
public interface ContentRepository<S extends Content> extends DataentityRepository<S> {
		
}
