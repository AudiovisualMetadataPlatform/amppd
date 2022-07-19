package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.indiana.dlib.amppd.model.MgmScoringParameter;

public interface MgmScoringParameterRepository extends PagingAndSortingRepository<MgmScoringParameter, Long> {
	
	// find all parameters of the given scoring tool
	List<MgmScoringParameter> findByMstId(Long mstId);

	// find the parameters of the given name the given scoring tool
	// since name is unique within MST, it's safe to findFirstBy
	MgmScoringParameter findFirstByMstIdAndName(Long mstId, String name);

}