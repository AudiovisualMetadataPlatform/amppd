package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.projection.DataentityBrief;

@NoRepositoryBean
//@RepositoryRestResource(excerptProjection = DataentityBrief.class)
public interface SupplementRepository<S extends Supplement> extends AssetRepository<S> {
}
