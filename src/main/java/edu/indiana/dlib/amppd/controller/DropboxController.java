package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.service.DropboxService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to handle dropbox related actions.
 * @author yingfeng
 *
 */
@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class DropboxController {
	
	@Autowired
	private DropboxService dropboxService;

	/**
	 * Create sub-directories as needed for the all existing collections.
	 */
	@PostMapping("/primaryfiles/{id}/upload")
	public Primaryfile uploadPrimaryfile(@PathVariable("id") Long id, @RequestParam("file") MultipartFile file) {		
    	log.info("Uploading media file " + file.getName() + " for primaryfile ID " + id);
    	return fileStorageService.uploadPrimaryfile(id, file);
    }
}
