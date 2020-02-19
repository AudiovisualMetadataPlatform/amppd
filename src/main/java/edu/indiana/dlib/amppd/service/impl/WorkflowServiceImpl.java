package edu.indiana.dlib.amppd.service.impl;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of WorkflowService.
 * @author yingfeng
 *
 */
@Service
@Slf4j
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
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.getWorkflow(String)
	 */	
	@Override
	public Workflow getWorkflow(String workflowName) {
		for (Workflow workflow : workflowsClient.getWorkflows()) {
			if (workflow.getName().equalsIgnoreCase(workflowName)) {
				return workflow;			
			}
		}
		return null;
	}
	
}
