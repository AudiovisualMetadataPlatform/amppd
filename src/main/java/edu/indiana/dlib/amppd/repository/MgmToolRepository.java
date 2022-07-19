package edu.indiana.dlib.amppd.repository;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.indiana.dlib.amppd.model.MgmTool;


public interface MgmToolRepository extends PagingAndSortingRepository<MgmTool, Long> {
	
	// find the MGM of the given toolId
	// since toolId is unique, it's safe to findFirstBy
	MgmTool findFirstByToolId(String toolId);
	
}
