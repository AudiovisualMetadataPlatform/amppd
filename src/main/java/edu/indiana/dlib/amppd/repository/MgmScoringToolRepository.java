package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.projection.MgmScoringToolBrief;


@RepositoryRestResource(excerptProjection = MgmScoringToolBrief.class)
public interface MgmScoringToolRepository extends MgmMetaRepository<MgmScoringTool> {
	
	// count the MSTs within the given category
	int countByCategoryId(Long categoryId);
	
	// find all scoring tools within the given category
	List<MgmScoringTool> findByCategoryId(Long categoryId);

	// find all scoring tools of the given workflowResultType and groundtruthFormat
	List<MgmScoringTool> findByWorkflowResultTypeAndGroundtruthFormat(String workflowResultType, String groundtruthFormat);

	// find the scoring tool of the given toolId;
	// since toolId is unique, it's safe to findFirstBy
	MgmScoringTool findFirstByToolId(String toolId);
	
	// find the scoring tool of the given name within the given category;
	// since name is unique within category, it's safe to findFirstBy
	MgmScoringTool findFirstByCategoryIdAndName(Long categoryId, String name);

	// delete obsolete record
	List<MgmScoringTool> deleteByModifiedDateBefore(Date dateObsolete);

}