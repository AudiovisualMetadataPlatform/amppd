package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.MgmTool;

public interface MgmToolRepository extends PagingAndSortingRepository<MgmTool, Long> {
	
	List<MgmTool> findByToolId(String toolId);
	
	@Query(value = "select m from MgmTool m where m.toolId = :toolId and m.upgradeDate < :invocationTime orderby upgradeDate desc")
	List<MgmTool> findLatestByToolId(String toolId, Date invocationTime);

}
