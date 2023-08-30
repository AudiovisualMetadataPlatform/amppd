package edu.indiana.dlib.amppd.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.ConfigService;
import edu.indiana.dlib.amppd.service.impl.ConfigServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on AMP configurations.
 * @author yingfeng
 */
@RestController
@Slf4j
public class ConfigController {

	@Autowired
	private ConfigService configService;

	
	// Note:
	// Configuration info provided in this controller are needed by UI and available to every one,
	// thus no authorization is needed.
	
	
	/**
	 * Return the requested configuration properties.
	 * @param properties name of the properties requested; null means all client visible properties.
	 * @return a map of property name-value pairs
	 */
	@GetMapping("/config")
	public Map<String,List<String>> getConfigProperties(@RequestParam(required = false) List<String> properties) {
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		
		// Currently, client visible config properties include supplementCategories, externalSources and taskManagers;
		// all other ones requested are ignored.
		if (properties == null || properties.contains(ConfigServiceImpl.SUPPLEMENT_CATEGORIES)) {
			log.info("Getting configuration property " + ConfigServiceImpl.SUPPLEMENT_CATEGORIES);
			map.put(ConfigServiceImpl.SUPPLEMENT_CATEGORIES, configService.getSupplementCategories());
		}
		if (properties == null || properties.contains(ConfigServiceImpl.EXTERNAL_SOURCES)) {
			log.info("Getting configuration property " + ConfigServiceImpl.EXTERNAL_SOURCES);
			map.put(ConfigServiceImpl.EXTERNAL_SOURCES, configService.getExternalSources());
		}
		if (properties == null || properties.contains(ConfigServiceImpl.TASK_MANAGERS)) {
			log.info("Getting configuration property " + ConfigServiceImpl.TASK_MANAGERS);
			map.put(ConfigServiceImpl.TASK_MANAGERS, configService.getTaskManagers());
		}
		
		return map;
	}
	
}
