package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.NoRepositoryBean;

import edu.indiana.dlib.amppd.model.Supplement;

@NoRepositoryBean
public interface SupplementRepository<S extends Supplement> extends AssetRepository<S> {
	
}
