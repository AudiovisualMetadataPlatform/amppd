package edu.indiana.dlib.amppd.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.BatchService;
import edu.indiana.dlib.amppd.service.BatchValidationService;
import edu.indiana.dlib.amppd.service.PreprocessService;
import edu.indiana.dlib.amppd.web.BatchValidationResponse;
import io.micrometer.core.instrument.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

//@CrossOrigin(origins = "*", allowedHeaders = "*")
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
    private PrimaryfileRepository primaryfileRepository;
	@Autowired
    private PreprocessService preprocessService;
	
	@PostMapping(path = "/batch/ingest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody BatchValidationResponse batchIngest(@RequestPart MultipartFile file, @RequestPart String unitName) {	
		
		// the user submitting the batch shall be the current user logged in and should be recorded in the user session
		AmpUser ampUser = ampUserService.getCurrentUser();
		
		BatchValidationResponse response = batchValidationService.validateBatch(unitName, ampUser, file);
		log.info("Batch validation success : "+response.isSuccess());
		if(response.isSuccess()) {
			response = batchService.processBatch(response, ampUser.getUsername());
			boolean batchSuccess = (!response.hasProcessingErrors());
			log.info("  errors:"+ response.getProcessingErrors().size());
			response.setSuccess(batchSuccess);
			log.info("Batch processing success : "+batchSuccess+" processing errors:"+response.getProcessingErrors());
		}
		
		return response;
	}

	/**
	 * Run preprocessing on existing primaryfiles missing media info due to previous failures.
	 * @param (optional) primaryfileId, if provispecifiedded, preprocess only this primaryfile, otherwise preprocess all as needed
	 * @return the list of primaryfiles failed to be preprocessed
	 */
	@GetMapping(path = "/batch/preprocess")
	public List<Primaryfile> preprocessPrimaryfilesMissingMediaInfo(@RequestParam(required = false) Long primaryfileId) {
		List<Primaryfile> primaryfiles = new ArrayList<Primaryfile>();
		List<Primaryfile> failedPfiles = new ArrayList<Primaryfile>();

		// if primaryfileId is specified, add the primaryfile for processing if it's missing mediaInfo
		if (primaryfileId != null) { 
			Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfileId + "> does not exist!"));
			if (StringUtils.isEmpty(primaryfile.getMediaInfo())) {
				primaryfiles.add(primaryfile);
				log.info("Preprocessing primaryfile " + primaryfileId + " missing mediaInfo ... " );
			}
			else {
				log.warn("The specified primaryfile " + primaryfileId + " has already been proprocessed, no need to re-process");
			}
		} 
		// otherwise find all primaryfiles missing mediaInfo
		else { 
			primaryfiles = primaryfileRepository.findByMediaInfoNull();
			log.info("Preprocessing " + primaryfiles.size() + " primaryfiles missing media info ...");
		}

		// preprocess above found primaryfiles
		for (Primaryfile primaryfile : primaryfiles) {
			try {
				preprocessService.preprocess(primaryfile, true);
				if (StringUtils.isEmpty(primaryfile.getMediaInfo())) {
					failedPfiles.add(primaryfile);
				}
			}
			catch (Exception e) {
				failedPfiles.add(primaryfile);
			}
		}

		int success = primaryfiles.size() - failedPfiles.size();		
		log.info("Successfully preprocessed " +  success + " primaryfiles missing media info, " + failedPfiles.size() + " primaryfiles still failed.");
		return failedPfiles;
	}
	
}
