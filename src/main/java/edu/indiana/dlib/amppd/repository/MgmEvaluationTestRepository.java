package edu.indiana.dlib.amppd.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest.TestStatus;

@RepositoryRestResource(exported = false)
public interface MgmEvaluationTestRepository extends PagingAndSortingRepository<MgmEvaluationTest, Long>, MgmEvaluationTestRepositoryCustom {
	
	List<MgmEvaluationTest> findByStatus(TestStatus status);
	List<MgmEvaluationTest> findByMstId(Long mstId);
	List<MgmEvaluationTest> findByGroundtruthSupplementId(Long supplementId);
	List<MgmEvaluationTest> findByWorkflowResultId(Long workflowResultId);

	List<MgmEvaluationTest> findByCategoryId(Long categoryId);

	@Query(value = "SELECT met FROM MgmEvaluationTest met WHERE id in :ids")
	List<MgmEvaluationTest> findByIds(@Param("ids") List<Long> idList);

}