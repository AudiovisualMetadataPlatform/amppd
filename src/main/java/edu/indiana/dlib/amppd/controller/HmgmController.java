package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.HmgmService;
import edu.indiana.dlib.amppd.web.NerEditorRequest;
import edu.indiana.dlib.amppd.web.NerEditorResponse;
import edu.indiana.dlib.amppd.web.SaveNerRequest;
import edu.indiana.dlib.amppd.web.SaveTranscriptRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorResponse;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class HmgmController {
		
	@Autowired HmgmService hmgmService;
	
	@GetMapping(path = "/hmgm/transcript-editor", produces = "application/json")
	public @ResponseBody TranscriptEditorResponse transcriptEditor(String datasetPath, boolean reset) {			
		return hmgmService.getTranscript(datasetPath, reset);
	}
	
	@PostMapping(path = "/hmgm/transcript-editor/save", consumes="application/json", produces = "application/json")
	public @ResponseBody boolean saveTranscript(@RequestBody SaveTranscriptRequest request) {			
		return hmgmService.saveTranscript(request);
	}
	
	@PostMapping(path = "/hmgm/transcript-editor/complete", consumes="application/json", produces = "application/json")
	public @ResponseBody boolean completeTranscript(@RequestBody TranscriptEditorRequest request) {			
		return hmgmService.completeTranscript(request);
	}
	
	@GetMapping(path = "/hmgm/ner-editor", produces = "application/json")
	public @ResponseBody NerEditorResponse nerEditor(String datasetPath, boolean reset) {			
		return hmgmService.getNer(datasetPath, reset);
	}
	
	@PostMapping(path = "/hmgm/ner-editor/save", consumes="application/json", produces = "application/json")
	public @ResponseBody boolean saveNer(@RequestBody SaveNerRequest request) {			
		return hmgmService.saveNer(request);
	}
	
	@PostMapping(path = "/hmgm/ner-editor/complete", consumes="application/json", produces = "application/json")
	public @ResponseBody boolean completeNer(@RequestBody NerEditorRequest request) {			
		return hmgmService.completeNer(request);
	}
	
}

