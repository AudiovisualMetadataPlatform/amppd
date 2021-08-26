package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.web.bind.annotation.CrossOrigin;

import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.projection.DataentityBrief;

@CrossOrigin(origins = "*")
//@RepositoryRestResource(excerptProjection = DataentityBrief.class)
//@RepositoryRestResource(collectionResourceRel = "primaryfileSupplements", path = "primaryfileSupplements")
public interface PrimaryfileSupplementRepository extends SupplementRepository<PrimaryfileSupplement> {

	List<PrimaryfileSupplement> findByPrimaryfileIdAndName(Long primaryfileId, String name);
	
}
