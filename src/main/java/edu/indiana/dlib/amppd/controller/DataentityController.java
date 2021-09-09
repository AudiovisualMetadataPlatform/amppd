package edu.indiana.dlib.amppd.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.impl.DataentityServiceImpl;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller for REST operations on Dataentity.
 * @author yingfeng
 */
@RestController
@Slf4j
public class DataentityController {
	
	@Autowired
	private DataentityService dataentityService;

	/**
	 * Return the requested configuration properties.
	 * @param properties name of the properties requested; null means all client visible properties.
	 * @return a map of property name-value pairs
	 */
	@GetMapping("/config")
	public Map<String,List<String>> getConfigProperties(@RequestParam(required = false) List<String> properties) {
		Map<String,List<String>> map = new HashMap<String,List<String>>();
		
		// Currently, client visible config properties include only externalSources and taskManagers;
		// all other ones requested are ignored.
		if (properties == null || properties.contains(DataentityServiceImpl.EXTERNAL_SOURCES)) {
			log.info("Getting configuration property " + DataentityServiceImpl.EXTERNAL_SOURCES);
			map.put(DataentityServiceImpl.EXTERNAL_SOURCES, dataentityService.getExternalSources());
		}
		if (properties == null || properties.contains(DataentityServiceImpl.TASK_MANAGERS)) {
			log.info("Getting configuration property " + DataentityServiceImpl.TASK_MANAGERS);
			map.put(DataentityServiceImpl.TASK_MANAGERS, dataentityService.getTaskManagers());
		}
		
		return map;
	}
	
	
	

}
