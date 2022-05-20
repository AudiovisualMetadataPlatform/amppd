package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.web.MgmEvaluationResponse;
import lombok.extern.slf4j.Slf4j;

//@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class MgmEvaluationController {

	@Autowired
	private MgmEvaluationService mgmEvaluationImpl;
	
	@PostMapping(path = "/evaluation/saveDetails")
	public boolean saveMgmEvaltion(@RequestParam("file") MultipartFile reapExcelDataFile) {			
		return mgmEvaluationImpl.saveMgmEvaluation(reapExcelDataFile);
	}
	
	@GetMapping(path = "/evaluation/getMgmEvaluationDetails", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody MgmEvaluationResponse getMgmEvaluationDetails() {		
		return mgmEvaluationImpl.getAllMgmEvaluationDetails();
	}
	
	
}
