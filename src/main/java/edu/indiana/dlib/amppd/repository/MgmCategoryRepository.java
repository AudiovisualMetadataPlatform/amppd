package edu.indiana.dlib.amppd.repository;

import edu.indiana.dlib.amppd.model.MgmCategory;

public interface MgmCategoryRepository extends AmpObjectRepository<MgmCategory> {
	
	// since name/sectionId is unique, it's safe to findFirstBy
	MgmCategory findFirstByName(String name);
	MgmCategory findFirstBySectionId(String sectionId);

}


