package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.BatchService;
import edu.indiana.dlib.amppd.service.BatchValidationService;
import edu.indiana.dlib.amppd.service.impl.BatchServiceImpl;
import edu.indiana.dlib.amppd.web.BatchValidationResponse;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class BatchController {
	@Autowired
	private BatchValidationService batchValidationService;
	@Autowired
	private BatchService batchService;
	@Autowired
	private AmpUserService ampUserService;
	@Autowired
	private AmppdPropertyConfig ampPropertyConfig;
	
	@PostMapping(path = "/batch/ingest", consumes = "multipart/form-data", produces = "application/json")
	public @ResponseBody BatchValidationResponse batchIngest(@RequestPart MultipartFile file, @RequestPart String unitName) {	
		
		// the user submitting the batch shall be the current user logged in and should be recorded in the user session
		AmpUser ampUser = ampUserService.getCurrentUser();
		
		BatchValidationResponse response = batchValidationService.validateBatch(unitName, ampUser, file);
		log.info("Batch validation success : "+response.isSuccess());
		if(response.isSuccess()) {
			List<String> errors = batchService.processBatch(response, ampUser.getUsername());
			boolean batchSuccess = errors.size()==0;
			log.info("  errors:"+ errors.size());
			response.setProcessingErrors(errors);
			response.setSuccess(batchSuccess);
			log.info("Batch processing success : "+batchSuccess+" processing errors:"+response.getProcessingErrors().size());
		}
		
		return response;
	}
}
