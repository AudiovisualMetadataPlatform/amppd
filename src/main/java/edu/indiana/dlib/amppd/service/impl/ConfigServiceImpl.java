package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.service.ConfigService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ConfigService.
 * @author yingfeng
 */
@Service
@Slf4j
public class ConfigServiceImpl implements ConfigService {

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
	
}
