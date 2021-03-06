package edu.indiana.dlib.amppd.repository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.Unit;

@CrossOrigin(origins = "*")
@RepositoryRestResource(collectionResourceRel = "units", path = "units")
public interface UnitRepository extends ContentRepository<Unit> {

}
