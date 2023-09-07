package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.DropboxService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to handle dropbox related actions.
 * @author yingfeng
 */
//@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class DropboxController {
	
	@Autowired
	private DropboxService dropboxService;

	/**
	 * Create dropbbox sub-directories as needed for all existing collections.
	 */
	// Disable endpoint not in use
//	@PostMapping("/dropbox/create")
	public void createCollectionSubdirs() {		
		/* 
		 * This endpoint was used to create dropbox subdirs for all collections retrospectively.
		 * We may not need this API anymore as each dropbox subdir is created when a new collection is created.
		 * Thus the permission for this API would be the same as Create Collection. 
		 */
    	log.info("Creating dropbox subdirectories for all existing collections ...");
    	dropboxService.createCollectionSubdirs();
    }
	
}
