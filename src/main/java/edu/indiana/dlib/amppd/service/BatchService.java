package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.web.BatchValidationResponse;
/**
 * Handles the processing of a Batch once it has been validated.  Processing includes creating all database
 * objects and moving files to the destination directory
 * @author dan
 *
 */
public interface BatchService {
	/**
	 * Takes in the result of a batch validation to create primary files, supplements, and also move files 
	 * to their destination directory
	 * 
	 * @param batchValidation
	 * @param username
	 * @return
	 */
	BatchValidationResponse processBatch(BatchValidationResponse batchValidation, String username);
}
