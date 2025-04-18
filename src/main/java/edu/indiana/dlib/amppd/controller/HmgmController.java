package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.HmgmAuthService;
import edu.indiana.dlib.amppd.service.HmgmNerService;
import edu.indiana.dlib.amppd.service.HmgmTranscriptService;
import edu.indiana.dlib.amppd.web.SaveTranscriptRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorResponse;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class HmgmController {
		
	@Autowired HmgmTranscriptService hmgmTranscriptService;
	@Autowired HmgmNerService hmgmNerService;
	@Autowired HmgmAuthService hmgmAuthService;
	
	/**
	 * Authorize/reject HMGM editor authentication request based on the client supplied HMGM token if provided, or otherwise,
	 * the combination of editor input file path, user entered editor password, and client supplied authentication string.   
	 * @param hmgmToken client supplied HMGM token
	 * @param editorInput editor input file path
	 * @param userPass user entered editor password
	 * @param authString client supplied authentication string
	 * @return the generated HMGM token for HMGM editor authentication if valid; null otherwise
	 */
	@GetMapping(path = "/hmgm/authorize-editor")
	public String authorizeEditor(
			@RequestParam(required = false) String hmgmToken,
			@RequestParam(required = false) String editorInput, 
			@RequestParam(required = false) String userPass, 
			@RequestParam(required = false) String authString) {	
		// if only editor input plus user password plus auth string are provided, validate them for authentication
		if (hmgmToken == null && editorInput != null && userPass != null && authString != null) {
			return hmgmAuthService.validateAuthString(editorInput, userPass, authString);
		}
		
		// otherwise if only HMGM token is provided, validate it for authentication
		if (hmgmToken != null && editorInput == null && userPass == null && authString == null) {
			return hmgmAuthService.validateHmgmToken(hmgmToken);
		}
		
		// otherwise if HMGM token is provided along with editorInput and authString, validate them for authentication
		if (hmgmToken != null && editorInput != null && userPass == null && authString != null) {
			return hmgmAuthService.validateHmgmTokenAuthString(hmgmToken, editorInput, authString);
		}

		// otherwise authentication fails
		return null;
	}
	
	@GetMapping(path = "/hmgm/transcript-editor", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody TranscriptEditorResponse getTranscript(@RequestParam String datasetPath, @RequestParam boolean reset) {		
		return hmgmTranscriptService.getTranscript(datasetPath, reset);
	}
	
	@PostMapping(path = "/hmgm/transcript-editor", consumes = MediaType.APPLICATION_JSON_VALUE)
	public boolean saveTranscript(@RequestBody SaveTranscriptRequest request) {			
		return hmgmTranscriptService.saveTranscript(request);
	}
	
	@PostMapping(path = "/hmgm/transcript-editor/complete", consumes = MediaType.APPLICATION_JSON_VALUE)
	public boolean completeTranscript(@RequestBody TranscriptEditorRequest request) {			
		return hmgmTranscriptService.completeTranscript(request);
	}
	
	@GetMapping(path = "/hmgm/ner-editor", produces = MediaType.APPLICATION_JSON_VALUE)
	public String getNer(@RequestParam String resourcePath) {		
		return hmgmNerService.getNer(resourcePath);
	}
	
	@PostMapping(path = "/hmgm/ner-editor", consumes = MediaType.APPLICATION_JSON_VALUE)
	public boolean saveNer(@RequestParam String resourcePath, @RequestBody String content) {			
		return hmgmNerService.saveNer(resourcePath, content);
	}
	
	@PostMapping(path = "/hmgm/ner-editor/complete")
	public boolean completeNer(@RequestParam String resourcePath) {			
		return hmgmNerService.completeNer(resourcePath);
	}
	
	@PostMapping(path = "/hmgm/ner-editor/reset")
	public boolean resetNer(@RequestParam String resourcePath) {			
		return hmgmNerService.resetNer(resourcePath);
	}	

}

