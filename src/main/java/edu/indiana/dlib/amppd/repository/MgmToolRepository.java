package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.MgmTool;


public interface MgmToolRepository extends AmpObjectRepository<MgmTool> {
	
	// find all scoring tools within the given category
	List<MgmScoringTool> findByCategoryId(Long categoryId);

	// find the MGM of the given toolId
	// since toolId is unique, it's safe to findFirstBy
	MgmTool findFirstByToolId(String toolId);

	// find the MGM of the given name within the given category;
	// since name is unique within category, it's safe to findFirstBy
	MgmScoringTool findFirstByCategoryIdAndName(Long categoryId, String name);

	// delete obsolete record
	List<MgmTool> deleteByModifiedDateBefore(Date dateObsolete);
	
}
