package edu.indiana.dlib.amppd.service;

import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.web.MgmEvaluationResponse;

/**
 * Service for MgmEvaluation details saving in DB.
 * @author Dhanunjaya
 */
public interface MgmEvaluationService {
	
	public boolean saveMgmEvaluation(MultipartFile file);
	
	public MgmEvaluationResponse getAllMgmEvaluationDetails();
	
}
