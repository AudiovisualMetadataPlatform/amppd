package edu.indiana.dlib.amppd.repository;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest.TestStatus;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MgmEvaluationTestRepository extends PagingAndSortingRepository<MgmEvaluationTest, Long> {
	
	List<MgmEvaluationTest> findByStatus(TestStatus status);
	List<MgmEvaluationTest> findByMstId(Long mstId);
	List<MgmEvaluationTest> findByGroundtruthSupplementId(Long supplementId);
	List<MgmEvaluationTest> findByWorkflowResultId(Long workflowResultId);

	List<MgmEvaluationTest> findByCategoryId(Long categoryId);

}