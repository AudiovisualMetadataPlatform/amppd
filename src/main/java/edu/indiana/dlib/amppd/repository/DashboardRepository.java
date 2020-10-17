package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.DashboardResult;

public interface DashboardRepository extends PagingAndSortingRepository<DashboardResult, Long>, DashboardRepositoryCustom {	
		
	List<DashboardResult> findByPrimaryfileId(Long primaryfileId);
	List<DashboardResult> findByPrimaryfileIdAndIsFinalTrue(Long primaryfileId);
	List<DashboardResult> findByOutputId(String outputId);
	
	@Query(value = "select case when count(*)>0 then true else false end from DashboardResult i where i.invocationId = :invocationId")
	boolean invocationExists(@Param("invocationId") String invocationId);
	
	@Query(value = "select min(date_refreshed) from DashboardResult d where d.primaryfileId = :primaryfileId")
	Date findOldestDateRefreshedByPrimaryfileId(Long primaryfileId);	
	
}
