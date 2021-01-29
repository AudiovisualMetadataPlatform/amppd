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
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of HmgmNerService.
 * @author yingfeng
 *
 */
@Service
@Slf4j
public class HmgmNerServiceImpl implements HmgmNerService {
	
	private String TMP_EXTENSION=".tmp";
	private String COMPLETE_EXTENSION=".complete";
	
	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmNerService.getNer(String)
	 */
	@Override
	public String getNer(String resourcePath) {			
		JSONParser parser = new JSONParser();
		String pathToFile = resourcePath;
		
		try {
			if (new File(pathToFile + COMPLETE_EXTENSION).exists()) {
				// TODO should throw exception?
				log.error("Error getting NER editor input: editor already completed with " + pathToFile);
				return null;
			}
			if (!new File(pathToFile).exists()) {
				log.error("Error getting NER editor input: file does not exist: " + pathToFile);
				return null;
			}
			
			File tempFile = new File(resourcePath + TMP_EXTENSION);
			if (tempFile.exists()) {
				pathToFile = tempFile.getAbsolutePath();
		        log.info("Temporary NER editor input file exists, using this version instead of the original input.");
			}
			
	        FileReader fileReader = new FileReader(pathToFile);
	        JSONObject json = (JSONObject) parser.parse(fileReader);
	        fileReader.close();
	        log.info("Successfully got NER editor input: " + pathToFile);
	        
	        return json.toJSONString();			
		} catch (IOException e) {
			log.error("Error reading NER editor input: " + pathToFile, e);
			return null;
		} catch (ParseException e) {
			log.error("Error parsing NER editor input when reading file: " + pathToFile, e);
			return null;
		}
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmNerService.saveNer(String, String)
	 */
	@Override
	public boolean saveNer(String resourcePath, String content) {			
		// do not allow save if the input file has already been completed with editing
		Path completePath = Paths.get(resourcePath + COMPLETE_EXTENSION);
		if (Files.exists(completePath)) {
			log.error("Error saving NER edits: the complete version of the input file already exists: " + completePath);
			return false;
		}

		JSONParser parser = new JSONParser();
		String tmpPath = resourcePath + TMP_EXTENSION;
		
		try {
	        JSONObject jsonTmp = (JSONObject) parser.parse(content);
	        
			try {
				FileWriter filewriter = new FileWriter(tmpPath);
				filewriter.write(jsonTmp.toJSONString());
				filewriter.close();
				log.info("Successfully saved NER editor content to file: " + tmpPath);				
				return true;
			} catch (IOException e) {
				log.error("Error saving NER editor content to file: " + tmpPath, e);
				return false;
			}
		} catch (ParseException e) {
			log.error("Error parsing NER editor content to JSON when saving file: " + tmpPath, e);
			return false;
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmNerService.completeNer(String)
	 */
	@Override
	public boolean completeNer(String resourcePath) {			
		// upon completion, HMGM tool expects the original file and .complete file exist as a result,
		// then it will delete the original file, and move the complete file to Galaxy
		Path srcPath = Paths.get(resourcePath);
		Path destPath = Paths.get(resourcePath + COMPLETE_EXTENSION);
		Path tmpPath = Paths.get(resourcePath + TMP_EXTENSION);
		
		// the original input file shall exist at this point
		if (!Files.exists(srcPath)) {
			log.error("Error completing NER edits: original source file does not exist: " + srcPath);
			return false;
		}		
		
		// the complete version of the input file shouldn't exist yet, as users are not allowed to resubmit
		if (Files.exists(destPath)) {
			log.error("Error completing NER edits: the complete version of the input file already exists: " + destPath);
			return false;
		}
		
		try {
			// if tmp file has been saved, move it to .complete file instead of copying, since upon completion, 
			// users are not allowed to edit the tmp file anymore, and HMGM tool doesn't expect it to exist 
			if (Files.exists(tmpPath)) {
				srcPath = tmpPath;
		        log.info("Temporary NER editor file exists, using this version.");
				Files.move(srcPath, destPath);
			}
			// otherwise, copy original file to .complete file instead of moving, as HMGM tool does expect it to exist 
			else {
		        log.info("Temporary NER editor file does not exist, using the original version.");
				Files.copy(srcPath, destPath);
			}
			
			log.info("Successfully completed NER edits into file: " + destPath);
			return true;
						
		} catch (Exception e) {
			log.error("Error completing NER edits into file: " + destPath, e);
			return false;
		}
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.HmgmNerService.resetNer(String)
	 */
	@Override
	public boolean resetNer(String resourcePath) {			
		Path tmpPath = Paths.get(resourcePath + TMP_EXTENSION);

		try {
			if (Files.exists(tmpPath)) {
				Files.delete(tmpPath);
				log.info("Successfully reset NER editor by deleting temporary file: " + tmpPath);
				return true;
			}
			else {
				log.warn("NER editor reset not done: temporary file has never been saved: " + tmpPath);
				return false;
			}						
		} catch (Exception e) {
			log.error("Error resetting NER editor temporary file: " + tmpPath, e);
			return false;
		}
	}
	
}
