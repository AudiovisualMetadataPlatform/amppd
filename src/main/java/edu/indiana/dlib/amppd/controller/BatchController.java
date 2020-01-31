package edu.indiana.dlib.amppd.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.BatchService;
import edu.indiana.dlib.amppd.service.BatchValidationService;
import edu.indiana.dlib.amppd.service.impl.AmpUserServiceImpl;
import edu.indiana.dlib.amppd.web.BatchValidationResponse;
import lombok.extern.java.Log;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Log 
public class BatchController {
	@Autowired
	private BatchValidationService batchValidationService;
	@Autowired
	private BatchService batchService;
	@Autowired
	private AmpUserService ampUserService;
	@Autowired
	private UnitRepository unitRepository;
	
	
	@PostMapping(path = "/batch/ingest", consumes = "multipart/form-data", produces = "application/json")
	public @ResponseBody BatchValidationResponse batchIngest(@RequestPart MultipartFile file, @RequestPart String unitName) {	
		
		AmpUser ampUser = new AmpUser();
		// create instance of Random class 
        Random rand = new Random(); 
  
        // Generate random integers in range 0 to 999 
        int rand_int1 = rand.nextInt(1000); 
		ampUser.setEmail("batchUser@iu.edu" + rand_int1);
		ampUser.setUsername("batchUser@iu.edu" + rand_int1);
		ampUser.setPassword("batchUser@iu.edu");
		ampUser.setApproved(true);
		ampUserService.registerAmpUser(ampUser);
		
		Unit unit = new Unit();
		
		List<Unit> units = unitRepository.findByName(unitName);
		
		if(units.size()>0) {
			unit = units.get(0);
		}
		else {
			unit.setName(unitName);
			unit.setModifiedBy("batchUser");
			unit.setCreatedBy("batchUser");
			unit.setModifiedDate(new Date().getTime());
			unit.setCreatedDate(new Date().getTime());
			unitRepository.save(unit);
		}
		
		
		BatchValidationResponse response = batchValidationService.validateBatch(unitName, ampUser, file);
		
		if(response.isSuccess()) {
			boolean batchSuccess = batchService.processBatch(response, ampUser.getUsername());
			response.setSuccess(batchSuccess);
		}
		
		return response;
	}
}
