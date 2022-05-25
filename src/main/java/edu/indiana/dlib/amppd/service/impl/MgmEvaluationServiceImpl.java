package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.MgmTool;
import edu.indiana.dlib.amppd.repository.MgmCategoryRepository;
import edu.indiana.dlib.amppd.repository.MgmScoringRepository;
import edu.indiana.dlib.amppd.repository.MgmToolRepository;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MgmEvaluationServiceImpl implements MgmEvaluationService {

	@Autowired
	MgmCategoryRepository mgmEvaluationCategory;

	@Autowired
	MgmScoringRepository mgmScoringRepository;

	@Autowired
	MgmToolRepository mgmToolRepository;

	
	/*
	 * @Override public MgmEvaluationResponse getAllMgmEvaluationDetails() {
	 * 
	 * List<MgmCategory> categoryList = (List<MgmCategory>)
	 * mgmEvaluationCategory.findAll();
	 * 
	 * return null; }
	 */
	
	@Override
	public boolean saveMgmEvaluation(MultipartFile category, MultipartFile scoring, MultipartFile tool) {
		
		String errmsg = "Failed to parse the input CSV file " + category.getOriginalFilename() + " for workflow submission!";
		
		parseCsv(category,scoring,tool);
		
		
		return false;
	}
	
	private void parseCsv(MultipartFile category,MultipartFile scoring,MultipartFile tool) {
		
		List<String[]> categoryRows = null;
		List<String[]> scoringRows = null;
		List<String[]> toolRows = null;
		int nrow = 0;
		int ncol = 0;
		try {
		CSVReader categorRreader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(category.getInputStream()))).build();
		categoryRows = categorRreader.readAll();
		
		CSVReader scoringRreader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(scoring.getInputStream()))).build();
		scoringRows = scoringRreader.readAll();
		
		CSVReader toolRreader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(tool.getInputStream()))).build();
		toolRows = toolRreader.readAll();
		
			
			for (int i=1; i < categoryRows.size(); i++) {
				String[] columns = categoryRows.get(i);
				inserDataToCategory(columns);				
			}
			
			for (int i=1; i < scoringRows.size(); i++) {
				String[] columns = scoringRows.get(i);
				inserDataToScoringTool(columns);				
			}
			for (int i=1; i < toolRows.size(); i++) {
				String[] columns = toolRows.get(i);
				inserDataToTool(columns);				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void inserDataToCategory(String[] columns) {
		int count = 1;
		MgmCategory category = new MgmCategory();
		for (int j=1; j < columns.length; j++) {
			
			 if(count == 1) {
				 category.setSectionId(columns[j]);
			 }
			 if(count == 2) {
				 category.setSectionName(columns[j]);
			 }
			 if(count == 3) {
				 category.setDescription(columns[j]);
			 }
			 count++;
		}
		
		mgmEvaluationCategory.save(category);
		
	}

	private void inserDataToScoringTool(String[] columns) {
		
		int count = 1;
		MgmScoringTool scoringTool = new MgmScoringTool();
		for (int j=1; j < columns.length; j++) {
			
			 if(count == 1) {
				 scoringTool.setName(columns[j]);
			 }
			 if(count == 2) {
				 scoringTool.setDescription(columns[j]);
			 }
			 if(count == 3) {
				 scoringTool.setVersion(columns[j]);
			 }
			 if(count == 4) {
				 scoringTool.setWorkflowResultDataType(columns[j]);
			 }
			 if(count == 5) {
				 scoringTool.setGroundTruthFormat(columns[j]);
			 }
			 if(count == 6) {
				 scoringTool.setParameters(columns[j] != null ? columns[j] : "{}");
			 }
			 if(count == 7) {
				 scoringTool.setScriptPath(columns[j]);
			 }
			 if(count == 9) {
				 scoringTool.setMgmToolId(columns[j]);
			 }
			 count++;
		}
				mgmScoringRepository.save(scoringTool);
			

	}

	private void inserDataToTool(String[] columns) {
		
		int count = 1;
		MgmTool tool = new MgmTool();
		for (int j=1; j < columns.length; j++) {
			
			 if(count == 1) {
				 tool.setToolId(columns[j]);
			 }
			 if(count == 2) {
				 tool.setMgmName(columns[j]);
			 }
			 tool.setUpgradeDate(new Date());
			 tool.setVersion("1.0");
			 count++;
		}
		mgmToolRepository.save(tool);

	}



	

	
}
