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

import edu.indiana.dlib.amppd.service.AuthService;
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
	@Autowired AuthService authService;
	
	@GetMapping(path = "/hmgm/authorize-editor")
	public String authorizeEditor(@RequestParam String editorInput, @RequestParam String userPass, @RequestParam String authString) {	
		return authService.validateAuthStrings(editorInput, userPass, authString);
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

