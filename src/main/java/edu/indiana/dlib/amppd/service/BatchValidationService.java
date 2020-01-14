package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.web.ValidationResponse;

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
	ValidationResponse validate(String unitName, String filename, AmpUser user, String fileContent);
}