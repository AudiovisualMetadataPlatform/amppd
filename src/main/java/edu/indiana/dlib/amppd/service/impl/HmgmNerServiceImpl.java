package edu.indiana.dlib.amppd.service.impl;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.service.HmgmNerService;
import edu.indiana.dlib.amppd.web.NerEditorResponse;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
/**
 * Implementation of HmgmNerService.
 * @author yingfeng
 *
 */
public class HmgmNerServiceImpl implements HmgmNerService {
	private String TEMP_EXTENSION=".tmp";
	private String COMPLETE_EXTENSION=".complete";
	
	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmNerService.getNer(String)
	 */
	@Override
	public String getNer(String resourcePath) {			
		JSONParser parser = new JSONParser();
		NerEditorResponse response = new NerEditorResponse();
		String pathToFile = resourcePath;
		try {
			if (new File(pathToFile + COMPLETE_EXTENSION).exists()) {
				// TODO should throw exception?
				log.error("Error getting NER input: editor already completed with " + pathToFile);
				return null;
			}
			if (!new File(pathToFile).exists()) {
				log.error("Error getting NER input: file does not exist: " + pathToFile);
				return null;
			}
			
			File tempFile = new File(resourcePath + TEMP_EXTENSION);
			if(tempFile.exists()) {
				pathToFile = tempFile.getAbsolutePath();
		        log.info("Temporary NER input file exists, using this version.");
			}
			
	        FileReader fileReader = new FileReader(pathToFile);
	        JSONObject json = (JSONObject) parser.parse(fileReader);
	        log.info("Successfully read NER input: " + pathToFile);
	        return json.toJSONString();			
		} catch (IOException e) {
			log.error("Error getting NER input: " + pathToFile, e);
			return null;
		} catch (ParseException e) {
			log.error("Error getting NER input: " + pathToFile, e);
			return null;
		}
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmNerService.saveNer(String, String)
	 */
	@Override
	public boolean saveNer(String resourcePath, String content) {			
		JSONParser parser = new JSONParser();
		String tmpPath = resourcePath + TEMP_EXTENSION;

		try {
	        JSONObject jsonTmp = (JSONObject) parser.parse(content);
	        
			try {
				FileWriter file = new FileWriter(tmpPath);
				file.write(jsonTmp.toJSONString());
				log.info("Successfully saved NER editor content to file: " + tmpPath);
			} catch (IOException e) {
				log.error("Error saving NER editor content to file: " + tmpPath, e);
				return false;
			}
			
			return true;
		} catch (ParseException e) {
			log.error("Error parsing NER editor content to JSON for file: " + tmpPath, e);
		}
		return false;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmNerService.completeNer(String)
	 */
	@Override
	public boolean completeNer(String resourcePath) {			
		Path srcPath = Paths.get(resourcePath);
		Path destPath = Paths.get(resourcePath + COMPLETE_EXTENSION);
		Path tmpPath = Paths.get(resourcePath + TEMP_EXTENSION);
		
		try {
			if (Files.exists(tmpPath)) {
				srcPath = tmpPath;
		        log.info("Temporary NER editor file exists, using this version.");
			}
			else if (!Files.exists(srcPath)) {
				log.error("Error completing NER editor with file: source file does not exist: " + srcPath);
				return false;
			}
			
			Files.copy(srcPath, destPath);
			log.info("Successfully completed NER editor with file: " + destPath);
			return true;
						
		} catch (Exception e) {
			log.error("Error completing NER editing with file: " + destPath, e);
			return false;
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmNerService.resetNer(String)
	 */
	@Override
	public boolean resetNer(String resourcePath) {			
		Path tmpPath = Paths.get(resourcePath + TEMP_EXTENSION);

		try {
			if (Files.exists(tmpPath)) {
				Files.delete(tmpPath);
				log.info("Successfully reset NER editor by deleting file: " + tmpPath);
				return true;
			}
			else {
				log.warn("NER editor reset not done: file has never been saved: " + tmpPath);
				return false;
			}						
		} catch (Exception e) {
			log.error("Error resetting NER editor with file: " + tmpPath, e);
			return false;
		}
	}
	
}
