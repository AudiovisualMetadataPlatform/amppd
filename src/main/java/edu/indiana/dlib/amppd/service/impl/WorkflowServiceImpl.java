package edu.indiana.dlib.amppd.service.impl;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;

import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import lombok.Getter;
import lombok.extern.java.Log;

/**
 * Implementation of WorkflowService.
 * @author yingfeng
 *
 */
@Service
@Log
public class WorkflowServiceImpl implements WorkflowService {
	
	@Autowired
	private GalaxyApiService galaxyApiService;
	
	@Getter
	private WorkflowsClient workflowsClient;
		
	/**
	 * Initialize the WorkflowServiceImpl bean.
	 */
	@PostConstruct
	public void init() {
		workflowsClient = galaxyApiService.getGalaxyInstance().getWorkflowsClient();
	}	
	
}
