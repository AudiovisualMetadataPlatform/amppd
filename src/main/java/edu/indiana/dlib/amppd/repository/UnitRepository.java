package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.Unit;

@CrossOrigin(origins = "*")
//@RepositoryRestResource(excerptProjection = DataentityBrief.class)
//@RepositoryRestResource(collectionResourceRel = "units", path = "units")
public interface UnitRepository extends DataentityRepository<Unit> {
	
}
