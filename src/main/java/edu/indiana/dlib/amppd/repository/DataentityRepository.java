package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.Dataentity;

@NoRepositoryBean
//@RepositoryRestResource(excerptProjection = DataentityBrief.class)
public interface DataentityRepository<S extends Dataentity> extends AmpObjectRepository<S> {
	
	List<S> findByName(@Param("name") String name);
	List<S> findByDescription(@Param("description") String description); // TODO: use customized impl to do match with SQL LIKE instead of =

}
