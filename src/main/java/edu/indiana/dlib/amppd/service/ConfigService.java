package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.Unit;

/**
 * Service for accessing AMP configurations. 
 * @author yingfeng
 */
public interface ConfigService {

	/**
	 * Return all supplement categories handled by AMP defined in application configuration,
	 * including all groundtruth subcategories if exist
	 * @return the array of allowed supplement categories
	 */
	public List<String> getSupplementCategories();
	
	/**
	 * Return all external sources supported by AMP defined in application configuration.
	 * @return the array of allowed external sources 
	 */
	public List<String> getExternalSources();
	
	/**
	 * Return all task managers supported by AMP defined in application configuration.
	 * @return the array of allowed task managers
	 */
	public List<String> getTaskManagers();

	/**
	 * Refresh units from its corresponding csv file.
	 * Note: This is a temporary workaround to initialize units, until we have UI to create/edit/delete units. 
	 */
	public List<Unit> refreshUnitTable();
	
}
