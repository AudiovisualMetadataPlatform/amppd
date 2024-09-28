package edu.indiana.dlib.amppd.controller;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.web.ItemSearchResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to handle requests for serving media files for primaryfiles and supplements.
 * @author yingfeng
 */
@RestController
@Slf4j
public class MediaController {

	@Autowired
	private PrimaryfileRepository primaryfileRepository;

	@Autowired
	private WorkflowResultRepository workflowResultRepository;
	
	@Autowired
    private MediaService mediaService;
	
	@Autowired
	private PermissionService permissionService;

	
	/**
	 * Get the media file symlink of the given primaryfile, which AMP clients can access without authentication.
	 * @param id ID of the given primaryfile
	 * @return the media file symlink
	 */
	@GetMapping("/primaryfiles/{id}/media")
	public String getPrimaryfileSymlink(@PathVariable("id") Long id) {
		Primaryfile primaryfile = primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));   

		// check permission 
		Long acUnitId = primaryfile.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Primaryfile_Media, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot play primaryfile media in unit " + acUnitId);
		}
		
		log.info("Serving media file for primaryfile ID " + id);
		String url = mediaService.getPrimaryfileSymlinkUrl(primaryfile);
		return url;
    }

	/**
	 * Get the output file symlink of the given workflowResult, which AMP clients can access without authentication.
	 * @param id ID of the given workflowResult
	 * @return the output file symlink
	 */
	@GetMapping("/workflowResults/{id}/output")
	public String getWorkflowOutputSymlink(@PathVariable("id") Long id) {		
		WorkflowResult workflowResult = workflowResultRepository.findById(id).orElseThrow(() -> new StorageException("workflowResultId <" + id + "> does not exist!"));   
		
		// check permission 
		Long acUnitId = workflowResult.getAcUnitId();
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.WorkflowResult_Output, acUnitId);
		if (!can) {
			throw new AccessDeniedException("The current user cannot view workflow result output in unit " + acUnitId);
		}
		
    	log.info("Serving output for workflowResult ID " + id);    	
    	String url = mediaService.getWorkflowResultOutputSymlinkUrl(workflowResult);
    	return url;
    }

//	/**
//	 * Serve the media file of the given primaryfile by redirecting the request to the AMPPD UI Tomcat server.
//	 * @param id ID of the given primaryfile
//	 * @return the binary content of the media file
//	 */
//	@GetMapping("/primaryfiles/{id}/media")
//	public ResponseEntity<Object> servePrimaryfile(@PathVariable("id") Long id) {
//		Primaryfile primaryfile = primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));   
//
//		// check permission 
//		Long acUnitId = primaryfile.getAcUnitId();
//		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.Primaryfile_Media, acUnitId);
//		if (!can) {
//			throw new AccessDeniedException("The current user cannot play primaryfile media in unit " + acUnitId);
//		}
//		
//		log.info("Serving media file for primaryfile ID " + id);
//		
//		String url = mediaService.getPrimaryfileSymlinkUrl(primaryfile);		
//    	HttpHeaders httpHeaders = new HttpHeaders();
//    	try {
//    		httpHeaders.setLocation(new URI(url));
//    	}
//    	catch (URISyntaxException e) {
//    		new RuntimeException("Invalid media symlink URL: " + url, e);
//    	}
//        
//    	return new ResponseEntity<>(httpHeaders, HttpStatus.PERMANENT_REDIRECT);
//    }
//
//	/**
//	 * Serve the output file of the given workflowResult by redirecting the request to the AMPPD UI Tomcat server.
//	 * @param id ID of the given workflowResult
//	 * @return the content of the output file
//	 */
//	@GetMapping("/workflowResults/{id}/output")
//	public ResponseEntity<Object> serveWorkflowOutput(@PathVariable("id") Long id) {		
//		WorkflowResult workflowResult = workflowResultRepository.findById(id).orElseThrow(() -> new StorageException("workflowResultId <" + id + "> does not exist!"));   
//		
//		// check permission 
//		Long acUnitId = workflowResult.getAcUnitId();
//		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.WorkflowResult_Output, acUnitId);
//		if (!can) {
//			throw new AccessDeniedException("The current user cannot view workflow result output in unit " + acUnitId);
//		}
//		
//    	log.info("Serving output for workflowResult ID " + id);
//    	
//    	String url = mediaService.getWorkflowResultOutputSymlinkUrl(workflowResult);    	
//    	HttpHeaders httpHeaders = new HttpHeaders();
//    	try {
//    		httpHeaders.setLocation(new URI(url));
//    	}
//    	catch (URISyntaxException e) {
//    		new RuntimeException("Invalid output symlink URL: " + url, e);
//    	}
//        
//    	return new ResponseEntity<>(httpHeaders, HttpStatus.PERMANENT_REDIRECT);
//    }
	
	/**
	 * Find items and/or primaryfiles with names containing the given keyword, and with media of the given media type,
	 * for the purpose of the given action if specified, defaults to Read Primaryfile otherwise.
	 * @param keyword the given keyword
	 * @param mediaType the given media type
	 * @param actionType actionType of the given action
	 * @param targetType targetType of the given action
	 * @return an instance of ItemSearchResponse containing information of the found items/primaryfiles
	 */
	@GetMapping(path = "/primaryfiles/search/findByKeywordMediaType")
	public @ResponseBody ItemSearchResponse searchItemFiles(
			@RequestParam("keyword") String keyword, 
			@RequestParam(required = false) String mediaType,
			@RequestParam(required = false) ActionType actionType, 
			@RequestParam(required = false) TargetType targetType) {							
		// if action not specified, default to Read WorkflowResult
		if (actionType == null && targetType == null) {
			actionType = ActionType.Read;
			targetType = TargetType.Primaryfile;
		}
		else if (actionType == null || targetType == null) {
			throw new IllegalArgumentException("The request parameters (actionType, targetType) must be both provided or both null!");			
		}
		
		/* Note: 
		 * Create WorkflowResult instead of Read Item/Primaryfile permission should be used
		 * if this search action is used for submitting PFiles to a workflow; 
		 * if AC is defined properly, the former is a stronger privilege than the latter.
		 * We need to make sure the search results are all good for workflow submission.
		 */		
		// get accessible units for Create WorkflowResult (if none, access denied exception will be thrown)
		// otherwise if accessibleUnits is null, i.e. user is admin, then no AC prefilter is needed; 
		// otherwise, all queries below are limited within the accessible units
		Set<Long> acUnitIds = permissionService.getAccessibleUnitIds(actionType, targetType);

		// TODO mediaType is currently not used by frontend as search criteria and may be removed from parameters
		log.info("Searching for items/primaryfiles: keywowrd = " + keyword + ", mediaType = " + mediaType);
		ItemSearchResponse res = new ItemSearchResponse();
		res = mediaService.searchItemFiles(keyword, mediaType, acUnitIds);
		return res;
	}
	
}
