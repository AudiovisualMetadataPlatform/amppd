package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.jmchilton.blend4j.galaxy.ToolsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import com.github.jmchilton.blend4j.galaxy.beans.ToolSection;
import com.opencsv.bean.CsvToBeanBuilder;

import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.MgmTool;
import edu.indiana.dlib.amppd.repository.MgmCategoryRepository;
import edu.indiana.dlib.amppd.repository.MgmScoringParameterRepository;
import edu.indiana.dlib.amppd.repository.MgmScoringToolRepository;
import edu.indiana.dlib.amppd.repository.MgmToolRepository;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.MgmRefreshService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of MgmRefreshService
 * @author yingfeng
 *
 */
@Service
@Slf4j
public class MgmRefreshServiceImpl implements MgmRefreshService {

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
		
	@Autowired
	private GalaxyApiService galaxyApiService;
	
	@Getter
	private ToolsClient toolsClient;

	
	/**
	 * Initialize the WorkflowServiceImpl bean.
	 */
	@PostConstruct
	public void init() {
		toolsClient = galaxyApiService.getGalaxyInstance().getToolsClient();
	}		
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MgmRefreshService.refreshMgmTables()
	 */
	@Override
    @Transactional
	public void refreshMgmTables() {
		refreshMgmCategory();
//		refreshMgmTool();
//		refreshMgmScoringTool();	
//		refreshMgmScoringParameter();	
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.MgmRefreshService.refreshMgmCategory()
	 */
	@Override
    @Transactional
	public List<MgmCategory> refreshMgmCategory() {
		log.info("Start refreshing MgmCategory table ...");
		List<MgmCategory> categories = new ArrayList<MgmCategory>();
		String filename = DIR + "/" + MGM_CATEGORY + ".csv"; 
		BufferedReader breader = null;
		
		// open mgm_category.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh MgmCategory table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of MgmCategory objects
		try {
			categories = new CsvToBeanBuilder<MgmCategory>(breader).withType(MgmCategory.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh MgmCategory table: invalid CSV format with " + filename, e);
		}
		
		// retrieve all tool sections from Galaxy and set up HashMap for them
		List<ToolSection> sections = toolsClient.getTools();
		HashMap<String, String> sectionMap = new HashMap<String, String>();
		for (ToolSection section : sections) {
			sectionMap.put(section.getId(), section.getName());
		}
				
		// record the refresh start time
		Date refreshStart = new Date();
		
		// save each of the MgmCategory objects, either creating a new one or updating the existing one
		for (MgmCategory category : categories) {
			String sectionId = category.getSectionId();
			
			// check that the corresponding section exists in Galaxy and report error if not
			String sectionName = sectionMap.get(sectionId);
			if (sectionName == null) {
				throw new RuntimeException("Failed to refresh MgmCategory table: Invalid category with non-existing section ID in CSV: " + category);
			}
			// otherwise populate category name from the section name
			category.setName(sectionName);
			
			// note: we can't just save all categories directly, as that would create new records in the table;
			// instead, we need to find each existing record if any based on ID and update it
			MgmCategory existCategory = mgmCategoryRepository.findFirstBySectionId(sectionId);			
			if (existCategory != null) {
				category.setId(existCategory.getId());				
			}
			mgmCategoryRepository.save(category);	
		}		
		
		// delete all obsolete categories, i.e. those not updated during this pass of refresh;
		// based on the assumption that the csv file includes all the categories we want to keep 
		List<MgmCategory> deletedCategories = mgmCategoryRepository.deleteByModifiedDateBefore(refreshStart);
		log.info("Deleted " + deletedCategories.size() + " obsolete categories older than current refresh start time at " + refreshStart);			
				
		log.info("Successfully refreshed " + categories.size() + " categories from " + filename);
		return categories;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.MgmRefreshService.refreshMgmTool()
	 */
	@Override
    @Transactional
	public List<MgmTool> refreshMgmTool() {
		log.info("Start refreshing MgmTool table ...");
		List<MgmTool> mgms = new ArrayList<MgmTool>();
		String filename = DIR + "/" + MGM_TOOL + ".csv"; 
		BufferedReader breader = null;
		
		// open mgm_tool.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh MgmTool table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of MgmTool objects
		try {
			mgms = new CsvToBeanBuilder<MgmTool>(breader).withType(MgmTool.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh MgmTool table: invalid CSV format with " + filename, e);
		}
				
		// record the refresh start time
		Date refreshStart = new Date();
		
		// save each of the MgmTool objects, either creating a new one or updating the existing one
		for (MgmTool mgm : mgms) {
			// check that the corresponding MGM adapter exists in Galaxy and report error if not
			Tool tool = toolsClient.showTool(mgm.getToolId());
			if (tool == null) {
				throw new RuntimeException("Failed to refresh MgmTool table: Invalid MGM with non-existing tool ID in CSV: " + mgm);
			}
			// otherwise populate MGM name from the tool name
			mgm.setName(tool.getName());
			
			// check that the tool's sectionId is valid for an existing category and report error if not
			String sectionId = tool.getSectionId();
			MgmCategory category = mgmCategoryRepository.findFirstBySectionId(sectionId);
			if (category == null) {
				throw new RuntimeException("Failed to refresh MgmTool table: Invalid MGM with non-existing category section ID " + sectionId + " in CSV: " + mgm);
			}
			// otherwise populate MGM's category
			mgm.setCategory(category);

			// note: we can't just save all mgms directly, as that would create new records in the table;
			// instead, we need to find each existing record if any based on ID and update it
			MgmTool existMgm = mgmToolRepository.findFirstByToolId(mgm.getToolId());
			if (existMgm != null) {
				mgm.setId(existMgm.getId());				
			}
			mgmToolRepository.save(mgm);	
		}		
		
		// delete all obsolete mgms, i.e. those not updated during this pass of refresh;
		// based on the assumption that the csv file includes all the mgms we want to keep 
		List<MgmTool> deletedMgms = mgmToolRepository.deleteByModifiedDateBefore(refreshStart);
		log.info("Deleted " + deletedMgms.size() + " obsolete MGMs older than current refresh start time at " + refreshStart);				
		
		log.info("Successfully refreshed " + mgms.size() + " MGMs from " + filename);
		return mgms;
	}
	
	/**
	 *  @see edu.indiana.dlib.amppd.service.MgmRefreshService.refreshMgmScoringTool()
	 */
	@Override
    @Transactional
	public List<MgmScoringTool> refreshMgmScoringTool() {
		log.info("Start refreshing MgmScoringTool table ...");
		List<MgmScoringTool> msts = new ArrayList<MgmScoringTool>();
		String filename = DIR + "/" + MGM_SCORING_TOOL + ".csv"; 
		BufferedReader breader = null;
		
		// open mgm_scoring_tool.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh MgmScoringTool table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of MgmScoringTool objects
		try {
			msts = new CsvToBeanBuilder<MgmScoringTool>(breader).withType(MgmScoringTool.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh MgmScoringTool table: invalid CSV format with " + filename, e);
		}
		
		// record the refresh start time
		Date refreshStart = new Date();
		
		// save each of the MgmScoringTool objects, either creating a new one or updating the existing one
		for (MgmScoringTool mst : msts) {
			// check that the sectionId is valid for an existing category and report error if not
			MgmCategory category = mgmCategoryRepository.findFirstBySectionId(mst.getSectionId());
			if (category == null) {
				throw new RuntimeException("Failed to refresh MgmScoringTool table: Invalid MST with non-existing section ID in CSV: " + mst);
			}
			// otherwise populate MST's category
			mst.setCategory(category);

			// note: we can't just save all msts directly, as that would create new records in the table;
			// instead, we need to find each existing record based on tool ID and update it
			MgmScoringTool existMst = mgmScoringToolRepository.findFirstByToolId(mst.getToolId());			
			if (existMst != null) {
				mst.setId(existMst.getId());				
			}			
			mgmScoringToolRepository.save(mst);	
		}		
		
		// delete all obsolete msts, i.e. those not updated during this pass of refresh;
		// based on the assumption that the csv file includes all the msts we want to keep 
		List<MgmScoringTool> deletedMsts = mgmScoringToolRepository.deleteByModifiedDateBefore(refreshStart);
		log.info("Deleted " + deletedMsts.size() + " obsolete MSTs older than current refresh start time at " + refreshStart);				
		
		log.info("Successfully refreshed " + msts.size() + " MSTs from " + filename);
		return msts;
	}
	
	/**
	 *  @see edu.indiana.dlib.amppd.service.MgmRefreshService.refreshMgmScoringParameter()
	 */
	@Override
    @Transactional
	public List<MgmScoringParameter> refreshMgmScoringParameter() {
		log.info("Start refreshing MgmScoringParameter table ...");
		List<MgmScoringParameter> parameters = new ArrayList<MgmScoringParameter>();
		String filename = DIR + "/" + MGM_SCORING_PARAMETER + ".csv"; 
		BufferedReader breader = null;
		
		// open mgm_scoring_parameter.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh MgmScoringParameter table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of MgmScoringParameter objects
		try {
			parameters = new CsvToBeanBuilder<MgmScoringParameter>(breader).withType(MgmScoringParameter.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh MgmScoringParameter table: invalid CSV format with " + filename, e);
		}
		
		// record the refresh start time
		Date refreshStart = new Date();
				
		// save each of the MgmScoringParameter objects, either creating a new one or updating the existing one
		for (MgmScoringParameter parameter : parameters) {
			// check that the scoring toolId is a valid existing one and report error if not
			MgmScoringTool mst = mgmScoringToolRepository.findFirstByToolId(parameter.getMstToolId());
			if (mst == null) {
				throw new RuntimeException("Failed to refresh MgmScoringParameter table: invalid parameter with non-existing MST toolId in CSV: " + parameter);
			}
			// otherwise populate the parameter's MST
			parameter.setMst(mst);

			// check that the dependency parameter is a valid existing one and report error if not;
			// note that the dependency parameter must appear before the current one in the CSV
			MgmScoringParameter dependency = mgmScoringParameterRepository.findFirstByMstIdAndName(mst.getId(), parameter.getDependencyName());
			if (dependency == null) {
				throw new RuntimeException("Failed to refresh MgmScoringParameter table: invalid parameter with non-existing dependencyName in CSV: " + parameter);
			}
			// otherwise populate the parameter's dependency
			parameter.setDependency(dependency);

			// note: we can't just save all parameters directly, as that would create new records in the table;
			// instead, we need to find each existing record based on ID and update it
			MgmScoringParameter existScoringParameter = mgmScoringParameterRepository.findFirstByMstIdAndName(mst.getId(), parameter.getName());			
			if (existScoringParameter != null) {
				parameter.setId(existScoringParameter.getId());				
			}			
			mgmScoringParameterRepository.save(parameter);	
		}		
		
		// delete all obsolete parameters, i.e. those not updated during this pass of refresh;
		// based on the assumption that the csv file includes all the mgms we want to keep 
		List<MgmScoringParameter> deletedParams = mgmScoringParameterRepository.deleteByModifiedDateBefore(refreshStart);
		log.info("Deleted " + deletedParams.size() + " obsolete parameters older than current refresh start time at " + refreshStart);				
		
		log.info("Successfully refreshed " + parameters.size() + " parameters from " + filename);
		return parameters;
	}
		
}
