package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.WorkflowResult;

public interface WorkflowResultRepository extends PagingAndSortingRepository<WorkflowResult, Long>, WorkflowResultRepositoryCustom {	
		
	List<WorkflowResult> findByPrimaryfileId(Long primaryfileId);
	List<WorkflowResult> findByPrimaryfileIdAndIsFinalTrue(Long primaryfileId);
	List<WorkflowResult> findByOutputId(String outputId);
	
	Set<WorkflowResult> findByWorkflowStepAndOutputName(String workflowStep, String outputName);
	Set<WorkflowResult> findByWorkflowStepIn(List<String> workflowSteps);
	Set<WorkflowResult> findByOutputNameIn(List<String> outputNames);

	Set<WorkflowResult> findByRelevant(Boolean relevant);
	Set<WorkflowResult> findByWorkflowIdAndRelevant(String workflowId, Boolean relevant);
	Set<WorkflowResult> findByWorkflowStepAndRelevant(String workflowStep, Boolean relevant);
	Set<WorkflowResult> findByOutputNameAndRelevant(String outputName, Boolean relevant);
	Set<WorkflowResult> findByWorkflowIdAndWorkflowStepAndRelevant(String workflowId, String workflowStep, Boolean relevant);
	Set<WorkflowResult> findByWorkflowIdAndOutputNameAndRelevant(String workflowId, String outputName, Boolean relevant);
	Set<WorkflowResult> findByWorkflowStepAndOutputNameAndRelevant(String workflowStep, String outputName, Boolean relevant);
	Set<WorkflowResult> findByWorkflowIdAndWorkflowStepAndOutputNameAndRelevant(String workflowId, String workflowStep, String outputName, Boolean relevant);

	List<WorkflowResult> findByPrimaryfileIdNotInAndDateRefreshedBefore(List<Long> primaryfileIds, Date dateObsolete);
	List<WorkflowResult> deleteByPrimaryfileIdNotInAndDateRefreshedBefore(List<Long> primaryfileIds, Date dateObsolete);
	List<WorkflowResult> deleteByDateRefreshedBefore(Date dateObsolete);

	List<WorkflowResult> deleteByCollectionId(Long id);
	
	@Query(value = "select case when count(*)>0 then true else false end from WorkflowResult i where i.invocationId = :invocationId")
	boolean invocationExists(@Param("invocationId") String invocationId);
	
	@Query(value = "select min(dateRefreshed) from WorkflowResult d where d.primaryfileId = :primaryfileId")
	Date findOldestDateRefreshedByPrimaryfileId(Long primaryfileId);
	
	@Query(value = "select d from WorkflowResult d where d.dateRefreshed < :dateObsolete")
	List<WorkflowResult> findObsolete(Date dateObsolete);
	
}
