package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.HmgmNerService;
import edu.indiana.dlib.amppd.service.HmgmTranscriptService;
import edu.indiana.dlib.amppd.web.SaveTranscriptRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorResponse;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class HmgmController {
		
	@Autowired HmgmTranscriptService hmgmTranscriptService;
	@Autowired HmgmNerService hmgmNerService;
	
	@GetMapping(path = "/hmgm/transcript-editor", produces = "application/json")
	public @ResponseBody TranscriptEditorResponse transcriptEditor(String datasetPath, boolean reset) {			
		return hmgmTranscriptService.getTranscript(datasetPath, reset);
	}
	
	@PostMapping(path = "/hmgm/transcript-editor/save", consumes="application/json", produces = "application/json")
	public @ResponseBody boolean saveTranscript(@RequestBody SaveTranscriptRequest request) {			
		return hmgmTranscriptService.saveTranscript(request);
	}
	
	@PostMapping(path = "/hmgm/transcript-editor/complete", consumes="application/json", produces = "application/json")
	public @ResponseBody boolean completeTranscript(@RequestBody TranscriptEditorRequest request) {			
		return hmgmTranscriptService.completeTranscript(request);
	}
	
	@GetMapping(path = "/hmgm/ner-editor", produces = "application/json")
	public @ResponseBody String getNer(@RequestParam String resourcePath) {		
		return hmgmNerService.getNer(resourcePath);
	}
	
	@PostMapping(path = "/hmgm/ner-editor", consumes = "application/json")
	public @ResponseBody boolean saveNer(@RequestParam String resourcePath, @RequestBody String content) {			
		return hmgmNerService.saveNer(resourcePath, content);
	}
	
	@PostMapping(path = "/hmgm/ner-editor/complete")
	public @ResponseBody boolean completeNer(@RequestParam String resourcePath) {			
		return hmgmNerService.completeNer(resourcePath);
	}
	
	@PostMapping(path = "/hmgm/ner-editor/reset")
	public @ResponseBody boolean resetNer(@RequestParam String resourcePath) {			
		return hmgmNerService.resetNer(resourcePath);
	}	

//	@PostMapping(path = "/hmgm/ner-editor/reset")
//	public @ResponseBody boolean resetNer(HttpServletRequest request, @RequestParam String resourcePath) {			
//		log.debug("Reset NER request URL: " + request.getRequestURI());
//		return hmgmNerService.resetNer(resourcePath);
//	}
//	
}

