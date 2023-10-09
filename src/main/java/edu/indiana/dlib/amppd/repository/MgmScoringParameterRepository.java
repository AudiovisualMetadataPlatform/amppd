package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.projection.MgmScoringParameterBrief;


@RepositoryRestResource(excerptProjection = MgmScoringParameterBrief.class)
public interface MgmScoringParameterRepository extends MgmMetaRepository<MgmScoringParameter> {
	
	// find all parameters of the given scoring tool
	@RestResource(exported = false)
	List<MgmScoringParameter> findByMstId(Long mstId);

	// find the parameters of the given name and the given scoring tool by ID;
	// since name is unique within MST, it's safe to findFirstBy
	@RestResource(exported = false)
	MgmScoringParameter findFirstByMstIdAndName(Long mstId, String name);

	// delete obsolete record
	@RestResource(exported = false)
	List<MgmScoringParameter> deleteByModifiedDateBefore(Date dateObsolete);
	
}