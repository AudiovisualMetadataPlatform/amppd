package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest.TestStatus;
import edu.indiana.dlib.amppd.model.projection.MgmEvaluationTestDetail;


public interface MgmEvaluationTestRepository extends PagingAndSortingRepository<MgmEvaluationTest, Long>, MgmEvaluationTestRepositoryCustom {
	
	List<MgmEvaluationTest> findByStatus(TestStatus status);
	List<MgmEvaluationTest> findByMstId(Long mstId);
	List<MgmEvaluationTest> findByGroundtruthSupplementId(Long supplementId);
	List<MgmEvaluationTest> findByWorkflowResultId(Long workflowResultId);
	List<MgmEvaluationTest> findByCategoryId(Long categoryId);

	List<MgmEvaluationTestDetail> findByIdIn(List<Long> ids);		
	List<MgmEvaluationTestDetail> findByIdInAndWorkflowResultUnitIdIn(List<Long> ids, Set<Long> acUnitIds);

}