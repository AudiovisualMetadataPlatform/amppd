package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import lombok.extern.slf4j.Slf4j;

//@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class MgmEvaluationController {

	@Autowired
	private MgmEvaluationService mgmEvaluationImpl;
	
	/*
	 * @GetMapping(path = "/evaluation/getMgmEvaluationDetails", produces =
	 * MediaType.APPLICATION_JSON_VALUE) public @ResponseBody MgmEvaluationResponse
	 * getMgmEvaluationDetails() { return
	 * mgmEvaluationImpl.getAllMgmEvaluationDetails(); }
	 */
	
	@PostMapping(path = "/evaluation/saveCSVDetails")
	public boolean saveMgmEvaltion(@RequestParam("ToolFile") MultipartFile toolCSVFile,@RequestParam("CategoryFile") MultipartFile CategoryCSVFile,@RequestParam("ScoringFile") MultipartFile scoringCSVFile) {			
		log.info("Processing request to save evaluation details into database... ");
		return mgmEvaluationImpl.saveMgmEvaluation(CategoryCSVFile,scoringCSVFile,toolCSVFile);
	}
	
	
}
