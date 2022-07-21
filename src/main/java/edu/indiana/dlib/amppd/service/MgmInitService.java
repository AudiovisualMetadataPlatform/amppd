package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.MgmTool;

/**
 * Service for initializing all MGM related tables.
 * @author yingfeng
 *
 */
public interface MgmInitService {

	/**
	 * Initialize MgmCategory table from mgm_category.csv file.
	 */
	public List<MgmCategory> initMgmCategory();

	/**
	 * Initialize MgmTool table from mgm_tool.csv file.
	 */
	public List<MgmTool> initMgmTool();
	
	/**
	 * Initialize MgmScoringTool table from mgm_scoring_tool.csv file.
	 */
	public List<MgmScoringTool> initMgmScoringTool();	

	/**
	 * Initialize MgmScoringParameter table from mgm_scoring_parameter.csv file.
	 */
	public List<MgmScoringParameter> initMgmScoringParameter();	
		
}
