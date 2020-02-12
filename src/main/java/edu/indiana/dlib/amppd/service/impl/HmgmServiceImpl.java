package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.service.HmgmService;
import edu.indiana.dlib.amppd.web.SaveTranscriptRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorResponse;
import lombok.extern.java.Log;

@Service
@Log
public class HmgmServiceImpl implements HmgmService {
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
			}
			File tempFile = new File(datasetPath + TEMP_EXTENSION);
			if(reset) {
				if(tempFile.exists()) {
					tempFile.delete();
				}
			}
			else {
				if(tempFile.exists()) {
					pathToFile = tempFile.getAbsolutePath();
					response.setTemporaryFile(true);
				}
			}
			
	        FileReader fileReader = new FileReader(pathToFile);
	        JSONObject json = (JSONObject) parser.parse(fileReader);
	        response.setContent(json.toJSONString());
	        response.setSuccess(true);
			
		} catch (IOException e) {
			e.printStackTrace();
			log.severe("Error getting transcript: " + e.getMessage());
		} catch (ParseException e) {
			log.severe("Error parsing transcript: " + e.toString());
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
				log.severe("Error converting transcript to json: " + e.getMessage());
				return false;
			}
			
			return true;
		} catch (ParseException e) {
			e.printStackTrace();
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
				return false;
			}
			
			File dest = new File(destFilePath);
			
			Files.copy(source.toPath(), dest.toPath());
			
			
		} catch (Exception e) {
			log.severe("Error completing transcript: " + e.getMessage());
			return false;
		}
		return true;
	}
}
