package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.indiana.dlib.amppd.model.MgmCategory;

public interface MgmCategoryRepository extends PagingAndSortingRepository<MgmCategory, Long> {
	
	// since name/sectionId is unique, it's safe to findFirstBy
	MgmCategory findFirstByName(String name);
	MgmCategory findFirstBySectionId(String sectionId);

}


