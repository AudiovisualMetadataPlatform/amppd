package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.format.annotation.DateTimeFormat;

import edu.indiana.dlib.amppd.model.MgmTool;


public interface MgmToolRepository extends PagingAndSortingRepository<MgmTool, Long> {
	
	List<MgmTool> findByToolId(String toolId);
	
	@Query(value = "select m from MgmTool m where m.toolId = :toolId and m.upgradeDate < :invocationTime order by upgradeDate desc")
	List<MgmTool> findLatestByToolId(String toolId, @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS") Date invocationTime);

}
