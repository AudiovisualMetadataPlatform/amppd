package edu.indiana.dlib.amppd.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service for MgmEvaluation details saving in DB.
 * @author Dhanunjaya
 */
public interface MgmEvaluationService {
	
	public boolean saveMgmEvaluation(MultipartFile category,MultipartFile scoring,MultipartFile tool);
	
	
}
