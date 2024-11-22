package edu.indiana.dlib.amppd.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class containing counts of children at various levels for a Dataentity. 
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataentityStatistics {

	Integer countCollections;
	Integer countItems;
	Integer countPrimaryfiles;
	Integer countUnitSupplements;
	Integer countCollectionSupplements;
	Integer countItemSupplements;
	Integer countPrimaryfileSupplements;
	Integer countWorkflowResults;
	Integer countMgmEvaluationTests;
	
}
