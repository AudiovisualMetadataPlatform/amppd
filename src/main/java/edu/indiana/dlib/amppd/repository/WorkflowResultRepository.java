package edu.indiana.dlib.amppd.repository;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.projection.PrimaryfileIdChain;
import edu.indiana.dlib.amppd.web.GalaxyJobState;


public interface WorkflowResultRepository extends PagingAndSortingRepository<WorkflowResult, Long>, WorkflowResultRepositoryCustom {	
		
	@Query(value = "select case when count(*)>0 then true else false end from WorkflowResult i where i.invocationId = :invocationId")
	boolean invocationExists(@Param("invocationId") String invocationId);
	
	// check processing status at various entity levels	
	boolean existsByUnitIdAndStatusIn(Long unitId, List<GalaxyJobState> statuses);
	boolean existsByCollectionIdAndStatusIn(Long collectionId, List<GalaxyJobState> statuses);
	boolean existsByItemIdAndStatusIn(Long itemId, List<GalaxyJobState> statuses);
	boolean existsByPrimaryfileIdAndStatusIn(Long primaryfileId, List<GalaxyJobState> statuses);
	
	// check processing status per workflow
	boolean existsByWorkflowIdAndStatusIn(String workflkowId, List<GalaxyJobState> statuses);
	
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

	List<WorkflowResult> findByCollectionIdInAndOutputTypeInAndStatusEqualsOrderByDateCreatedDesc(List<Long> collectionIds, List<String> outputTypes, GalaxyJobState status);
	List<WorkflowResult> findByPrimaryfileIdNotInAndDateRefreshedBefore(List<Long> primaryfileIds, Date dateObsolete);	
	
	List<WorkflowResult> deleteByPrimaryfileIdNotInAndDateRefreshedBefore(List<Long> primaryfileIds, Date dateObsolete);
	List<WorkflowResult> deleteByDateRefreshedBefore(Date dateObsolete);
	
	List<WorkflowResult> deleteByUnitId(Long unitId);
	List<WorkflowResult> deleteByCollectionId(Long collectionId);
	List<WorkflowResult> deleteByItemId(Long itemId);
	List<WorkflowResult> deleteByPrimaryfileId(Long primaryfileId);
	
	int countByUnitId(Long unitId);
	int countByCollectionId(Long collectionId);
	int countByItemId(Long itemId);
	int countByPrimaryfileId(Long primaryfileId);
	
	@Query(value = "select min(dateRefreshed) from WorkflowResult d where d.primaryfileId = :primaryfileId")
	Date findOldestDateRefreshedByPrimaryfileId(Long primaryfileId);
	
	@Query(value = "select d from WorkflowResult d where d.dateRefreshed < :dateObsolete")
	List<WorkflowResult> findObsolete(Date dateObsolete);
	
	// find results of the given primaryfile, outputType, and status
	List<WorkflowResult> findByPrimaryfileIdAndOutputTypeAndStatus(Long primaryfileId, String outputType, GalaxyJobState status);
	
	// find results with the given list of statuses
	List<WorkflowResult> findByStatusIn(List<GalaxyJobState> statuses);
	
	// find workflow IDs with results in the given list of statuses
	@Query(value = "select workflowId from WorkflowResult where status in :statuses")
	Set<String> findWorkflowIdsByStatusIn(List<GalaxyJobState> statuses);
	
	// count the distinct output types existing in the table within the given outputTypes list, and accessible units if not null
	// Note: for some reason Spring JPA doesn't work correctly with count distinct, thus the query is provided
	@Query(value = "select count(distinct outputType) from WorkflowResult where outputType in :outputTypes")
	int countDistinctOutputTypes(List<String> outputTypes);
	@Query(value = "select count(distinct outputType) from WorkflowResult where outputType in :outputTypes and unitId in :acUnitIds")
	int countDistinctOutputTypesAC(List<String> outputTypes, Set<Long> acUnitIds);
	
	// find primaryfiles with existing outputs for each of the given outputTypes, if each of the outputTypes exist in the table
	// Mote: if keyword is empty, the SQL below will ignore keyword matching, which is the desired behavior for our use case
	@Query(value = 
			"select distinct p.unitId as unitId, p.collectionId as collectionId, p.itemId as itemId, p.primaryfileId as primaryfileId " + 
			"from WorkflowResult p " + 
			"where lower(p.primaryfileName) like lower(concat('%', :keyword,'%')) " +
			"and not exists ( " + 
			"	select distinct o.outputType " + 
			"	from WorkflowResult o " + 
			"	where o.outputType in :outputTypes " +  
			"	and not exists ( " + 
			"		select w.id " + 
			"		from WorkflowResult w " + 
			"		where w.primaryfileId = p.primaryfileId " + 
			"		and w.outputType = o.outputType " + 
			"		and w.status = 'COMPLETE' " + 
			"	) " + 
			") " + 
			"order by p.collectionId, p.itemId, p.primaryfileId "
	)
	List<PrimaryfileIdChain> findPrimaryfileIdsByOutputType(String keyword, List<String> outputTypes);
	@Query(value = 
			"select distinct p.unitId as unitId, p.collectionId as collectionId, p.itemId as itemId, p.primaryfileId as primaryfileId " + 
			"from WorkflowResult p " + 
			"where p.unitId in :acUnitIds " +
			"and lower(p.primaryfileName) like lower(concat('%', :keyword,'%')) " +
			"and not exists ( " + 
			"	select distinct o.outputType " + 
			"	from WorkflowResult o " + 
			"	where o.outputType in :outputTypes " +  
			"	and not exists ( " + 
			"		select w.id " + 
			"		from WorkflowResult w " + 
			"		where w.primaryfileId = p.primaryfileId " + 
			"		and w.outputType = o.outputType " + 
			"		and w.status = 'COMPLETE' " + 
			"	) " + 
			") " + 
			"order by p.collectionId, p.itemId, p.primaryfileId "
	)
	List<PrimaryfileIdChain> findPrimaryfileIdsByOutputTypeAC(String keyword, List<String> outputTypes, Set<Long> acUnitIds);

//	// find primaryfiles with existing outputs for each of the given outputTypes, if each of the outputTypes exist in the table
//	// Mote: if keyword is empty, the SQL below will ignore keyword matching, which is the desired behavior for our use case
//	@Query(value = 
//			"select distinct p.collectionId as collectionId, p.collectionName as collectionName, " + 
//			"p.itemId as itemId, p.itemName as itemName, " +
//			"p.primaryfileId as primaryfileId, p.primaryfileName as primaryfileName " + 
//			"from WorkflowResult p " + 
//			"where lower(p.primaryfileName) like lower(concat('%', :keyword,'%')) " +
//			"and not exists ( " + 
//			"	select distinct o.outputType " + 
//			"	from WorkflowResult o " + 
//			"	where o.outputType in :outputTypes " +  
//			"	and not exists ( " + 
//			"		select w.id " + 
//			"		from WorkflowResult w " + 
//			"		where w.primaryfileId = p.primaryfileId " + 
//			"		and w.outputType = o.outputType " + 
//			"		and w.status = 'COMPLETE' " + 
//			"	) " + 
//			") " + 
//			"order by p.collectionId, p.itemId, p.primaryfileId "
//	)
//	List<PrimaryfileIdName> findPrimaryfileIdNamesByOutputTypes(String keyword, List<String> outputTypes);

	
// below query is correct in logic but won't work with JPA, which doesn't support subquery in FROM clause
//		select p.collectionName, p.itemName, p.primaryfileName, p.primaryfileId 
//		from (
//			select w.collectionName, w.itemName, w.primaryfileName, w.primaryfileId, w.outpuType 
//			from WorkflowResult w 
//			where w.outputType in :outputTypes 
//			group by w.collectionName, w.itemName, w.primaryfileName, w.primaryfileId, w.outpuType
//			) p
//		group by p.collectionName, p.itemName, p.primaryfileName, p.primaryfileId
//		having count(*) = :outputType.size()
//		order by p.primaryfileId
//		@Query(value = "select p.collectionName, p.itemName, p.primaryfileName, p.primaryfileId from (select w.collectionName, w.itemName, w.primaryfileName, w.primaryfileId, w.outputType from WorkflowResult w where w.outputType in :outputTypes group by w.collectionName, w.itemName, w.primaryfileName, w.primaryfileId, w.outpuType) p group by p.collectionName, p.itemName, p.primaryfileName, p.primaryfileId having count(*) = :outputTypes.size() order by p.primaryfileId")
//
// below query is correct in logic but not accepted by JPA syntax, with the "o(outputType)" part
//		@Query(value = 
//		"select distinct p.collectionName as collectionName, p.itemName as itemName, p.primaryfileName as primaryfileName, p.primaryfileId as primaryfileId " + 
//		"from WorkflowResult p " + 
//		"where not exists ( " + 
//		"	select * from unnest(:outputTypes) o(outputType) " +  
//		"	where not exists ( " + 
//		"		select w.id " + 
//		"		from WorkflowResult w " + 
//		"		where w.primaryfileId = p.primaryfileId " + 
//		"		and w.outputType = o.outputType " + 
//		"		and w.status = 'COMPLETE' " + 
//		"	) " + 
//		") "
//)		
//
// below query is correct in logic but not accepted by JPA syntax, with the ":outputTypes.size()" part
//	@Query(value = 
//	"select distinct p.collectionName as collectionName, p.itemName as itemName, p.primaryfileName as primaryfileName, p.primaryfileId as primaryfileId " + 
//	"from WorkflowResult p " + 
//	"where (select count(distinct outputType) from WorkflowResult where outputType in :outputTypes) = :outputTypes.size() " + 
//	"and not exists ( " + 
//	"	select distinct o.outputType " + 
//	"	from WorkflowResult o " + 
//	"	where o.outputType in :outputTypes " +  
//	"	and not exists ( " + 
//	"		select w.id " + 
//	"		from WorkflowResult w " + 
//	"		where w.primaryfileId = p.primaryfileId " + 
//	"		and w.outputType = o.outputType " + 
//	"		and w.status = 'COMPLETE' " + 
//	"	) " + 
//	") "
//)	
// below querry works with JPA, but the logic is not quite the desired one:
// it returns all primaryfiles that have some results for at least one instead for each outputType in the given list 
//		@Query(value = "select distinct w.collectionName, w.itemName, w.primaryfileName, w.primaryfileId from WorkflowResult w where w.outputType in :outputTypes")  

}
