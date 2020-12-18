package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.WorkflowResult;

public interface WorkflowResultRepository extends PagingAndSortingRepository<WorkflowResult, Long>, WorkflowResultRepositoryCustom {	
		
	List<WorkflowResult> findByPrimaryfileId(Long primaryfileId);
	List<WorkflowResult> findByPrimaryfileIdAndIsFinalTrue(Long primaryfileId);
	List<WorkflowResult> findByOutputId(String outputId);
	
	@Query(value = "select case when count(*)>0 then true else false end from WorkflowResult i where i.invocationId = :invocationId")
	boolean invocationExists(@Param("invocationId") String invocationId);
	
	@Query(value = "select min(dateRefreshed) from WorkflowResult d where d.primaryfileId = :primaryfileId")
	Date findOldestDateRefreshedByPrimaryfileId(Long primaryfileId);
	
	@Query(value = "select d from WorkflowResult d where d.dateRefreshed < :dateObsolete")
	List<WorkflowResult> findObsolete(Date dateObsolete);
	
}
