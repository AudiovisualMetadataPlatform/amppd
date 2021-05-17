package edu.indiana.dlib.amppd.controller;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.BatchService;
import edu.indiana.dlib.amppd.service.BatchValidationService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.PreprocessService;
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
    private PrimaryfileRepository primaryfileRepository;
	@Autowired
    private PreprocessService preprocessService;
	@Autowired
    private FileStorageService fileStorageService;
	
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
	 * Note: This is temporary and can be removed/disabled when batch ingest is fixed
	 *
	 * Run preprocessing on existing primary files to create and set media info
	 * @param (optional) primaryfileId, if included, will preprocess specified file,
	 * 	if not included will preprocess all files that are missing mediainfo
	 * @return string reporting initial file count, success count, and failure count
	 */
	@GetMapping(path = "/batch/preprocess")
	public String runPreprocessingOnFilesWithoutMediaInfo(@RequestParam(value = "primaryfileId", required = false) Long primaryfileId) {
		List<Primaryfile> primaryfileList = new ArrayList<Primaryfile>();

		if (primaryfileId != null) { // if primaryfileId is provided get primary file record
			log.info("Re-runnning preprocessing on primaryfileId: " + primaryfileId + " ... " );
			Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfileId + "> does not exist!"));
			primaryfileList.add(primaryfile);
		} else { // if no primaryfileId is provided, get all primary files
			log.info("Re-runnning preprocessing on all primary files without media info...");
			List<Primaryfile> primaryfiles = (List<Primaryfile>)primaryfileRepository.findAll();
			primaryfileList.addAll(primaryfiles);
		}

		// create counters for total initial files missing media info and media info creation successes
		int missingMediaInfo = 0;
		int success = 0;

		for (Primaryfile pf : primaryfileList) {
			if (pf.getMediaInfo() != null) continue; // skip files that already contain media info

			missingMediaInfo += 1;

			Asset updatedPrimaryfile = preprocessService.retrieveMediaInfo(pf);
			String mediaInfo = updatedPrimaryfile.getMediaInfo();

			if (StringUtils.isNotEmpty(mediaInfo)) success += 1;
		}

		return String.format("Files initially missing media info: %s\nFiles with media info successfully added: %s\nFiles still missing media info: %s", missingMediaInfo, success, (missingMediaInfo - success));
	}
	
	/**
	 * Serve the  fileContent  from resources/static Folder based on given fileName.
	 * @param fileName 
	 * @return the  content of the  file
	 */
	  @GetMapping("/download/{fileName:.+}")
	    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) throws Exception{
		    
		    String resourcesStaticFilePath= "static/"+fileName;
			ClassLoader classLoader = ClassLoader.getSystemClassLoader();
			URL url =classLoader.getResource(resourcesStaticFilePath);
			if(url==null) {
				return ResponseEntity.notFound().build();
			}
			File file = new File(url.getFile());
			Resource resource =fileStorageService.loadAsResource(file.getPath());
			String headerKey = "Content-Disposition";
			String headerValue = "attachment; filename="+fileName;
			log.info("Serving " + headerValue);
			return ResponseEntity.ok().header(headerKey,headerValue).body(resource);
				
	  }	
  
}
