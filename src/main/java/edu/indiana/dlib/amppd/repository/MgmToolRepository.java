package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.indiana.dlib.amppd.model.MgmTool;


public interface MgmToolRepository extends PagingAndSortingRepository<MgmTool, Long> {
	
	List<MgmTool> findByToolId(String toolId);
	
}
