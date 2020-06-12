package edu.indiana.dlib.amppd.repository;

import java.util.List;

import javax.persistence.EntityManager;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.web.DashboardResult;
import edu.indiana.dlib.amppd.web.DashboardSearchQuery;

public interface DashboardRepository  extends PagingAndSortingRepository<DashboardResult, Long>, DashboardRepositoryCustom{

	@Query(value = "select case when count(*)>0 then true else false end from DashboardResult i where i.invocationId = :invocationId")
	boolean invocationExists(@Param("invocationId") String invocationId);
		
	
}
