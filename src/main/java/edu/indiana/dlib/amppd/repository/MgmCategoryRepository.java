package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.projection.MgmCategoryBrief;


@RepositoryRestResource(excerptProjection = MgmCategoryBrief.class)
public interface MgmCategoryRepository extends MgmMetaRepository<MgmCategory> {
	
	// since name/sectionId is unique, it's safe to findFirstBy
	MgmCategory findFirstByName(String name);
	MgmCategory findFirstBySectionId(String sectionId);

	// delete obsolete record
	List<MgmCategory> deleteByModifiedDateBefore(Date dateObsolete);
	
}


