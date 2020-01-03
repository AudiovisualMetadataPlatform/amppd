package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.web.ValidationResponse;

public interface ManifestValidationService {
	ValidationResponse validate(String unitName, String fileContent);
}