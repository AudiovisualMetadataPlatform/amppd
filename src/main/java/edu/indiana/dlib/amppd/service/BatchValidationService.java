package edu.indiana.dlib.amppd.service;

import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.web.BatchResponse;

/**
 * Validates a batch manifest CSV returning any and all errors as strings 
 * @author dan
 *
 */
public interface BatchValidationService {
	/**
	 * Validates a csv file as a string and returns a batch as well as any and all errors with the manifest
	 * 
	 * @param unitName
	 * @param fileContent
	 * @return
	 */
	BatchResponse validate(String unitName, String filename, AmpUser user, String fileContent);
	BatchResponse validateBatch(String unitName, AmpUser user, MultipartFile file);
}