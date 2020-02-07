package edu.indiana.dlib.amppd.controller;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.LibrariesClient;
import com.github.jmchilton.blend4j.galaxy.SearchClient.SearchResponse;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContents;
import com.github.jmchilton.blend4j.galaxy.beans.Job;
import com.github.jmchilton.blend4j.galaxy.beans.Library;
import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;

import edu.indiana.dlib.amppd.service.GalaxyApiService;
import lombok.Getter;
import lombok.extern.java.Log;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Log 
public class HumanMgmController {
	

	public static final String SHARED_LIBARY_NAME = "Amppd Library";
	public static final String SHARED_HISTORY_NAME = "Amppd History";
	
	@Autowired
	private GalaxyApiService galaxyApiService;
	
	private GalaxyInstance galaxyInstance;
	
	@Getter
	private LibrariesClient librariesClient;
	
	
	@GetMapping(path = "/hmgm/transcript-editor", produces = "application/json")
	public @ResponseBody String transcriptEditor(String datasetPath) {	
		

		galaxyInstance = galaxyApiService.getGalaxyInstance();
		librariesClient = galaxyInstance.getLibrariesClient();
		Job t = galaxyInstance.getJobsClient().showJob(id);
		t.
		HistoriesClient historiesClient = galaxyInstance.getHistoriesClient();
		try {

			History matchingHistory = null;
			List<History> histories = historiesClient.getHistories();
			for (History curr : histories)
			{
				if (SHARED_HISTORY_NAME.equals(curr.getName()))
				{
					matchingHistory = curr;
				}
			}
			
			if(matchingHistory!=null) {

				// gets all contents stored within this library
				// converts to HashMap mapping file name to Galaxy library object since I find it easier to work with
				List<HistoryContents> libraryContentsList = historiesClient.showHistoryContents(matchingHistory.getId());
				for(HistoryContents content : libraryContentsList) {
					System.out.println(content.getName() + ":" + content.getUrl());
					
				}
			}
			
		}
		catch(Exception ex) {
			System.out.println(ex.getMessage());
		}
		
		JSONParser parser = new JSONParser();
		try {
	        FileReader fileReader = new FileReader(datasetPath);
	        JSONObject json = (JSONObject) parser.parse(fileReader);
			return json.toJSONString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
	}
	public void getStepOutput(String workflowId, Long primaryfileId, String stepId) {
	}
}

