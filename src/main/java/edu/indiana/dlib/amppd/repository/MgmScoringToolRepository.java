package edu.indiana.dlib.amppd.repository;

import java.util.List;

import edu.indiana.dlib.amppd.model.MgmScoringTool;

public interface MgmScoringToolRepository extends AmpObjectRepository<MgmScoringTool> {
	
	// find all scoring tools within the given category
	List<MgmScoringTool> findByCategoryId(Long categoryId);

	// find the scoring tool of the given name within the given category
	// since name is unique within category, it's safe to findFirstBy
	MgmScoringTool findFirstByCategoryIdAndName(Long categoryId, String name);

}