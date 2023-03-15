package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.projection.UnitBrief;

@RepositoryRestResource(excerptProjection = UnitBrief.class)
public interface UnitRepository extends DataentityRepository<Unit> {
	
	Unit findFirstByName(String name);
	UnitBrief findByIdProjectedBy(Long id);
	List<UnitBrief> findAllProjectedBy();
	
}
