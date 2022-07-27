package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import edu.indiana.dlib.amppd.model.MgmScoringTool;

public interface MgmScoringToolRepository extends AmpObjectRepository<MgmScoringTool> {
	
	// find all scoring tools within the given category
	List<MgmScoringTool> findByCategoryId(Long categoryId);

	// find the scoring tool of the given name 
	// since name is unique, it's safe to findFirstBy
	MgmScoringTool findFirstByName(Long categoryId, String name);

	// delete obsolete record
	List<MgmScoringTool> deleteByModifiedDateBefore(Date dateObsolete);

}