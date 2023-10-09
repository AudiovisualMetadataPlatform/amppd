package edu.indiana.dlib.amppd.repository;

import java.util.Set;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.projection.UnitBrief;

@RepositoryRestResource(excerptProjection = UnitBrief.class)
public interface UnitRepository extends DataentityRepository<Unit> {
	
	@RestResource(exported = false)
	Unit findFirstByName(String name);
	
	@RestResource(exported = false)
	Set<UnitBrief> findBy();
	
}
