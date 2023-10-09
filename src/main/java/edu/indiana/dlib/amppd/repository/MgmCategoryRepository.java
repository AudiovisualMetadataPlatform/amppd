package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.projection.MgmCategoryBrief;


@RepositoryRestResource(excerptProjection = MgmCategoryBrief.class)
public interface MgmCategoryRepository extends MgmMetaRepository<MgmCategory> {

	// since name/sectionId is unique, it's safe to findFirstBy

	@RestResource(exported = false)
	MgmCategory findFirstByName(String name);
	
	@RestResource(exported = false)
	MgmCategory findFirstBySectionId(String sectionId);

	// delete obsolete record
	@RestResource(exported = false)
	List<MgmCategory> deleteByModifiedDateBefore(Date dateObsolete);
	
}


