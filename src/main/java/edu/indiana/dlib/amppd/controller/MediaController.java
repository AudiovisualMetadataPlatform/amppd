package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.MediaService;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to handle requests for serving media files for primaryfiles and supplements.
 * @author yingfeng
 *
 */
@CrossOrigin(origins = "*")
@RestController
@Slf4j
public class MediaController {

	@Autowired
    private MediaService mediaService;

	/**
	 * Serve the media file of the given primaryfile by redirecting the request to the AMPPD UI Apache server.
	 * @param id ID of the given primaryfile
	 * @return the binary content of the media file
	 */
	@GetMapping("/primaryfiles/{id}/media")
	public String servePrimaryfile(@PathVariable("id") Long id) {		
    	log.info("Serving media file for primaryfile ID " + id);
    	String url = mediaService.getPrimaryfileSymlinkUrl(id);
    	return "redirect: " + url;
    }

}
