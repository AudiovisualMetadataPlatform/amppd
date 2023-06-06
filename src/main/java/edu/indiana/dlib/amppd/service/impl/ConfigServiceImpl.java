package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.bean.CsvToBeanBuilder;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.ConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ConfigService.
 * @author yingfeng
 */
@Service
@Slf4j
public class ConfigServiceImpl implements ConfigService {

	public static final String DIR = "db";
	public static final String UNIT = "unit";

	public static final String GROUNDTRUTH_CATEGORY_PREFIX = "Groundtruth"; 	
	public static final String SUPPLEMENT_CATEGORIES = "supplementCategories";
	public static final String EXTERNAL_SOURCES = "externalSources";
	public static final String TASK_MANAGERS = "taskManagers";

	@Value("#{'${amppd.supplementCategories}'.split(',')}")
	private List<String> supplementCategories;
	
	@Value("#{'${amppd.groundtruthSubcategories}'.split(',')}")
	private List<String> groundtruthSubcategories;

	@Value("#{'${amppd.externalSources}'.split(',')}")
	private List<String> externalSources;
	
	@Value("#{'${amppd.taskManagers}'.split(',')}")
	private List<String> taskManagers;
	
	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;
	
	@Autowired
	private UnitRepository unitRepository;

	
	/**
	 * Get the groundtruth category for the given subcategory.
	 * @param groundtruthSubcategory the given groundtruth subcategory
	 * @return the concatenated groundtruth category in the form of Groundtruth-subcategory
	 */
	public static String getGroundtruthCategory(String groundtruthSubcategory) {
		return GROUNDTRUTH_CATEGORY_PREFIX + "-" + groundtruthSubcategory;
	}
	
	/**
	 * Get the groundtruth subcategory for the given category.
	 * @param groundtruthCategory the given groundtruth category
	 * @return the subcategory stripped off the groundtruth cateogry prefix
	 */
	public static String getGroundtruthSubCategory(String groundtruthCategory) {
		return StringUtils.substringAfter(groundtruthCategory, GROUNDTRUTH_CATEGORY_PREFIX + "-");
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getSupplementCategories()
	 */
	public List<String> getSupplementCategories() {
		List<String> categories = new ArrayList<String>();
		categories.addAll(supplementCategories);

		// if exist, add the groundtruth subcategories to the supplement categories list, 
		// each in the form of Groundtruth-subcategory  
		if (groundtruthSubcategories != null) {
			for (String subcategory : groundtruthSubcategories) {
				categories.add(getGroundtruthCategory(subcategory));
			}
		}
		
		log.info("Successfully found " + categories.size() + " supplement categories.");
		return categories;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getExternalSources()
	 */
	public List<String> getExternalSources() {
		return externalSources;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getTaskManagers()
	 */
	public List<String> getTaskManagers() {
		return taskManagers;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.ConfigService.refreshUnitTable()
	 */
	@Override
    @Transactional
	public List<Unit> refreshUnitTable() {
		if (!amppdPropertyConfig.getRefresUnitTable()) return null; 
		
		log.info("Start refreshing Unit table ...");
		List<Unit> units = new ArrayList<Unit>();
		List<Unit> newUnits = new ArrayList<Unit>();
		String filename = DIR + "/" + UNIT + ".csv"; 
		BufferedReader breader = null;
		
		// open unit.csv
		try {
			breader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()));
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Unit table: unable to open " + filename, e);
		}		
		
		// parse the csv into list of Unit objects
		try {
			units = new CsvToBeanBuilder<Unit>(breader).withType(Unit.class).build().parse();
		}
		catch(Exception e) {
			throw new RuntimeException("Failed to refresh Unit table: invalid CSV format with " + filename, e);
		}
		
		// save each of the Unit objects, either creating a new one or updating the existing one
		for (Unit unit : units) {
			// note: we can't just save all units directly, as that would create new records in the table;
			// instead, we need to find each existing record if any based on name and update it
			Unit newUnit = unitRepository.findFirstByName(unit.getName());			
			if (newUnit == null) {
				newUnit = new Unit();
				newUnit.setName(unit.getName());
				log.debug("Adding new unit " + newUnit.getName());
			}	
			else {
				log.debug("Updating existing unit " + newUnit.getName());				
			}
			
			newUnit.setDescription(unit.getDescription());				
			newUnit.setTaskManager(unit.getTaskManager());							
			newUnit = unitRepository.save(newUnit);
			newUnits.add(newUnit);
		}		
		
		// don't delete any existing units not included in the csv, as we might already have some manually created ones
				
		log.info("Successfully refreshed " + newUnits.size() + " units from " + filename);
		return newUnits;
	}
	
}
