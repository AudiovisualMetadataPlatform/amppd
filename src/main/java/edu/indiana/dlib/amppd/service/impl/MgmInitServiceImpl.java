package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.opencsv.bean.CsvToBeanBuilder;

import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.MgmTool;
import edu.indiana.dlib.amppd.repository.MgmCategoryRepository;
import edu.indiana.dlib.amppd.repository.MgmScoringParameterRepository;
import edu.indiana.dlib.amppd.repository.MgmScoringToolRepository;
import edu.indiana.dlib.amppd.repository.MgmToolRepository;
import edu.indiana.dlib.amppd.service.MgmInitService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MgmInitService
 * @author yingfeng
 *
 */
@Service
@Slf4j
public class MgmInitServiceImpl implements MgmInitService {

	public static final String DIR = "db";
	public static final String MGM_CATEGORY = "mgm_category";
	public static final String MGM_TOOL = "mgm_tool";
	public static final String MGM_SCORING_TOOL = "mgm_scoring_tool";
	public static final String MGM_SCORING_PARAMETER = "mgm_scoring_parameter";
	 
	@Autowired
	private MgmCategoryRepository mgmCategoryRepository;
	
	@Autowired
	private MgmToolRepository mgmToolRepository;
	
	@Autowired
	private MgmScoringToolRepository mgmScoringToolRepository;
	
	@Autowired
	private MgmScoringParameterRepository mgmScoringParameterRepository;
	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MgmInitService.initMgmCategory()
	 */
	@Override
	public List<MgmCategory> initMgmCategory() {
		List<MgmCategory> categories = new ArrayList<MgmCategory>();
		String filename = DIR + "/" + MGM_CATEGORY + ".csv"; 
		BufferedReader breader = null;
		
		// open mgm_category.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to initialize MgmCategory table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of MgmCategory objects
		try {
			categories = new CsvToBeanBuilder<MgmCategory>(breader).withType(MgmCategory.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to initialize MgmCategory table: invalid CSV format with " + filename, e);
		}
		
		// save the MgmCategory objects
		mgmCategoryRepository.saveAll(categories);		
		
		log.info("Successfully initialized MgmCategory table from " + filename);
		return categories;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.MgmInitService.initMgmTool()
	 */
	@Override
	public List<MgmTool> initMgmTool() {
		List<MgmTool> mgms = new ArrayList<MgmTool>();
		String filename = DIR + "/" + MGM_TOOL + ".csv"; 
		BufferedReader breader = null;
		
		// open mgm_tool.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to initialize MgmTool table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of MgmTool objects
		try {
			mgms = new CsvToBeanBuilder<MgmTool>(breader).withType(MgmTool.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to initialize MgmTool table: invalid CSV format with " + filename, e);
		}
		
		// save the MgmTool objects
		mgmToolRepository.saveAll(mgms);		
		
		log.info("Successfully initialized MgmTool table from " + filename);
		return mgms;
	}
	
	/**
	 *  @see edu.indiana.dlib.amppd.service.MgmInitService.initMgmScoringTool()
	 */
	@Override
	public List<MgmScoringTool> initMgmScoringTool() {
		List<MgmScoringTool> msts = new ArrayList<MgmScoringTool>();
		String filename = DIR + "/" + MGM_SCORING_TOOL + ".csv"; 
		BufferedReader breader = null;
		
		// open mgm_scoring_tool.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to initialize MgmScoringTool table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of MgmScoringTool objects
		try {
			msts = new CsvToBeanBuilder<MgmScoringTool>(breader).withType(MgmScoringTool.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to initialize MgmScoringTool table: invalid CSV format with " + filename, e);
		}
		
		// save the MgmScoringTool objects
		mgmScoringToolRepository.saveAll(msts);		
		
		log.info("Successfully initialized MgmScoringTool table from " + filename);
		return msts;
	}
	
	/**
	 *  @see edu.indiana.dlib.amppd.service.MgmInitService.initMgmScoringParameter()
	 */
	@Override
	public List<MgmScoringParameter> initMgmScoringParameter() {
		List<MgmScoringParameter> parameters = new ArrayList<MgmScoringParameter>();
		String filename = DIR + "/" + MGM_SCORING_PARAMETER + ".csv"; 
		BufferedReader breader = null;
		
		// open mgm_scoring_parameter.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to initialize MgmScoringParameter table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of MgmScoringParameter objects
		try {
			parameters = new CsvToBeanBuilder<MgmScoringParameter>(breader).withType(MgmScoringParameter.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to initialize MgmScoringParameter table: invalid CSV format with " + filename, e);
		}
				
		// save the MgmScoringParameter objects
		mgmScoringParameterRepository.saveAll(parameters);		
		
		log.info("Successfully initialized MgmScoringParameter table from " + filename);
		return parameters;
	}
	
	
}
