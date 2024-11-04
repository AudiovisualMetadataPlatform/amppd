package edu.indiana.dlib.amppd.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest.TestStatus;
import edu.indiana.dlib.amppd.model.projection.MgmEvaluationTestDetail;


public interface MgmEvaluationTestRepository extends PagingAndSortingRepository<MgmEvaluationTest, Long>, MgmEvaluationTestRepositoryCustom {
	
	List<MgmEvaluationTest> findByStatus(TestStatus status);
	List<MgmEvaluationTest> findByMstId(Long mstId);
	List<MgmEvaluationTest> findByGroundtruthSupplementId(Long groundtruthSupplementId);
	List<MgmEvaluationTest> findByWorkflowResultId(Long workflowResultId);
	List<MgmEvaluationTest> findByCategoryId(Long categoryId);

	List<MgmEvaluationTestDetail> findByIdIn(List<Long> ids);		
	List<MgmEvaluationTestDetail> findByIdInAndWorkflowResultUnitIdIn(List<Long> ids, Set<Long> acUnitIds);

	@Query(value = "select m from MgmEvaluationTest m where m.groundtruthSupplement.primaryfile.id = :primaryfileId")
	List<MgmEvaluationTest> findByPrimaryfileId(Long primaryfileId);

	@Query(value = "select m from MgmEvaluationTest m where m.groundtruthSupplement.primaryfile.item.id = :itemId")
	List<MgmEvaluationTest> findByItemId(Long itemId);

	@Query(value = "select m from MgmEvaluationTest m where m.groundtruthSupplement.primaryfile.item.collection.id = :collectionId")
	List<MgmEvaluationTest> findByCollectionId(Long collectionId);

	@Query(value = "select m from MgmEvaluationTest m where m.groundtruthSupplement.primaryfile.item.collection.unit.id = :unitId")
	List<MgmEvaluationTest> findByUnitId(Long unitId);

}