package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.service.AuthService;
import edu.indiana.dlib.amppd.service.HmgmTranscriptService;
import edu.indiana.dlib.amppd.web.SaveTranscriptRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class HmgmTranscriptServiceImpl implements HmgmTranscriptService {
	private String TEMP_EXTENSION=".tmp";
	private String COMPLETE_EXTENSION=".complete";
	
	
	/*
	 * Get the json of a transcript
	 */
	public TranscriptEditorResponse getTranscript(String datasetPath, boolean reset) {	
		JSONParser parser = new JSONParser();
		TranscriptEditorResponse response = new TranscriptEditorResponse();
		try {
			String pathToFile = datasetPath;
			if(new File(pathToFile + COMPLETE_EXTENSION).exists()) {
				response.setComplete(true);
				log.error("Error getting transcript editor input: editor already completed with " + pathToFile);
				return response;
			}
			if(!new File(pathToFile).exists()) {
				log.error("Error getting transcript editor input: file does not exist: " + pathToFile);
				return response;
			}
			File tempFile = new File(datasetPath + TEMP_EXTENSION);
			if (reset) {
				if(tempFile.exists()) {
					tempFile.delete();
					log.info("Successfully reset transcript editor by deleting temporary file: " + tempFile);
				}
				else {
					log.warn("Transcript editor reset not done: temporary file has never been saved: " + tempFile);
				}
			}
			else if (tempFile.exists()) {
				pathToFile = tempFile.getAbsolutePath();
				response.setTemporaryFile(true);
				log.info("Temporary transcript editor input file exists, using this version instead of the original input.");				
			}
			
	        FileReader fileReader = new FileReader(pathToFile);
	        JSONObject json = (JSONObject) parser.parse(fileReader);
	        response.setContent(json.toJSONString());
	        response.setSuccess(true);
	        log.info("Successfully got transcript editor input: " + pathToFile);
		} catch (IOException e) {
			log.error("Error getting transcript editor input: " + datasetPath, e);
		} catch (ParseException e) {
			log.error("Error getting transcript editor input: failed to parse transcript JSON: " + datasetPath, e);
		}
		return response;
	}

	/*
	 * Save a temporary copy of the transcript in draftjs format
	 */
	public boolean saveTranscript(SaveTranscriptRequest request) {			
		JSONParser parser = new JSONParser();
		try {
	        JSONObject jsonTmp = (JSONObject) parser.parse(request.getJson());
	        
			try (FileWriter file = new FileWriter(request.getFilePath())) {
				file.write(jsonTmp.toJSONString());
			} catch (IOException e) {
				log.error("Error converting and saving transcript to json: " + request.getFilePath(), e);
				return false;
			}
			log.info("Successfully saved transcript editor content to JSON file: " + request.getFilePath());							
			return true;
		} catch (ParseException e) {
			log.error("Error saving transcript editor content: failed to parse JSON content for file " + request.getFilePath(), e);
		}
		return false;
	}
	
	/*
	 * Complete the transcript by copying latest file to file with .complete extention 
	 */
	public boolean completeTranscript(TranscriptEditorRequest request) {			
		try {
			int lastIndex = request.getFilePath().lastIndexOf(TEMP_EXTENSION);
			String destFilePath = "";
			if(lastIndex > 0) {
				destFilePath = request.getFilePath().substring(0, lastIndex) + COMPLETE_EXTENSION;				
			}
			else {
				destFilePath = request.getFilePath() + COMPLETE_EXTENSION;
			}
			
			File source = new File(request.getFilePath());
			if(!source.exists()) {
				source = new File(request.getFilePath().substring(0, lastIndex));
			}
			
			if(!source.exists()) {
				log.error("Error completing transcript edits: the original source file does not exist: " + request.getFilePath());
				return false;
			}
			
			File dest = new File(destFilePath);

			// the result is that we have the original file and the complete file
			if(lastIndex > 0) {
				// move tmp file to complete file
				Files.move(source.toPath(), dest.toPath());
			}
			else {
				// copy original file to complete file
				Files.copy(source.toPath(), dest.toPath());
			}			
			log.info("Successfully completed transcript edits into file: " + destFilePath);			
		} catch (Exception e) {
			log.error("Error completing transcript edits: " + request.getFilePath(), e);
			return false;
		}
		
		return true;
	}
	
}
