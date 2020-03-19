package edu.indiana.dlib.amppd.controller;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
	public ResponseEntity<Object> servePrimaryfile(@PathVariable("id") Long id) {		
    	log.info("Serving media file for primaryfile ID " + id);
    	String url = mediaService.getPrimaryfileSymlinkUrl(id);
    	HttpHeaders httpHeaders = new HttpHeaders();
    	try {
    		httpHeaders.setLocation(new URI(url));
    	}
    	catch (URISyntaxException e) {
    		new RuntimeException("Invalid media symlink URL: " + url, e);
    	}
        return new ResponseEntity<>(httpHeaders, HttpStatus.PERMANENT_REDIRECT);
    }
	
//	public String servePrimaryfile(@PathVariable("id") Long id) {		
//    	log.info("Serving media file for primaryfile ID " + id);
//    	String url = mediaService.getPrimaryfileSymlinkUrl(id);
//    	return "redirect: " + url;
//    }

}
