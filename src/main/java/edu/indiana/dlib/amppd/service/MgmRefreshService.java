package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.MgmCategory;
import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.MgmTool;

/**
 * Service for refreshing all MGM related tables.
 * @author yingfeng
 *
 */
public interface MgmRefreshService {

	/**
	 * Refresh all MGM tables in the appropriate order.
	 * Note that refreshing a parent table might impact its child tables, which in turn might need a refresh 
	 * (for ex, MgmCategory is parent of MgmTool and MgmScoringTool, and MgmScoringTool is parent of MgmScoringParameter).
	 */
	public void refreshMgmTables();
	
	/**
	 * Refresh MgmCategory table from mgm_category.csv file.
	 * @return the list of MgmCategories refreshed
	 */
	public List<MgmCategory> refreshMgmCategory();

	/**
	 * Refresh MgmTool table from mgm_tool.csv file.
	 * @return the list of MgmTools refreshed
	 */
	public List<MgmTool> refreshMgmTool();
	
	/**
	 * Refresh MgmScoringTool table from mgm_scoring_tool.csv file.
	 * @return the list of MgmScoringTools refreshed
	 */
	public List<MgmScoringTool> refreshMgmScoringTool();	

	/**
	 * Refresh MgmScoringParameter table from mgm_scoring_parameter.csv file.
	 * @return the list of MgmScoringParameters refreshed
	 */
	public List<MgmScoringParameter> refreshMgmScoringParameter();	
		
	/**
	 * Populate dependency parameters of all MgmScoringTools based on the parameter names
	 * @return the list of MgmScoringTools populated with valid dependency parameters
	 */
	public List<MgmScoringTool> populateMgmScoringToolDependencyParameters(List<MgmScoringTool> msts);	
		
}
