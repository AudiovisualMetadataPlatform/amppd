package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.repository.PagingAndSortingRepository;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest.TestStatus;

public interface MgmEvaluationTestRepository extends PagingAndSortingRepository<MgmEvaluationTest, Long> {
	
	List<MgmEvaluationTest> findByStatus(TestStatus status);
	List<MgmEvaluationTest> findByMstId(Long mstId);
	List<MgmEvaluationTest> findBySupplementId(Long supplementId);
	List<MgmEvaluationTest> findByWorkflowResultId(Long workflowResultId);

}