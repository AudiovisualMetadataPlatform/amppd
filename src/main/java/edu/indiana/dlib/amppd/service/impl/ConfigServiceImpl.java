package edu.indiana.dlib.amppd.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of ConfigService.
 * @author yingfeng
 */
@Service
@Slf4j
public class ConfigServiceImpl {

	public static final String SUPPLEMENT_CATEGORIES = "supplementCategories";
	public static final String EXTERNAL_SOURCES = "externalSources";
	public static final String TASK_MANAGERS = "taskManagers";

	@Value("#{'${amppd.supplementCategories}'.split(',')}")
	private List<String> supplementCategories;
	
	@Value("#{'${amppd.externalSources}'.split(',')}")
	private List<String> externalSources;
	
	@Value("#{'${amppd.taskManagers}'.split(',')}")
	private List<String> taskManagers;
	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DataentityService.getSupplementCategories()
	 */
	public List<String> getSupplementCategories() {
		return supplementCategories;
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
