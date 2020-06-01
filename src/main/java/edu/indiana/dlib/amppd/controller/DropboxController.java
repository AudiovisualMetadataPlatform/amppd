package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
	 * Create dropbbox sub-directories as needed for all existing collections.
	 */
	@PostMapping("/dropbox/create")
	public void createCollectionSubdirs() {		
    	log.info("Creating dropbox subdirectories for all existing collections ...");
    	dropboxService.createCollectionSubdirs();
    }
	
}
