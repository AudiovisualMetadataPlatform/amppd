package edu.indiana.dlib.amppd.service.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

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
	
	// use hashmap to cache workflow names to avoid frequent query request to Galaxy when refreshing workflow results
	private Map<String, String> workflowNames = new HashMap<String, String>();
			
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
		
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.getWorkflowName(String)
	 */
	public String getWorkflowName(String workflowId) {
		String workflowName = workflowNames.get(workflowId);		
		if (workflowName != null) return workflowName;
		
		try {
			WorkflowDetails workflow = workflowsClient.showWorkflowInstance(workflowId);
			if (workflow != null) {
				workflowName = workflow.getName();
			}
			else {
				// a workflow may not be found in Galaxy if its ID is not a StoredWorkflow ID;
				// in this case, use the ID itself as a temporary solution.
				// TODO this issue may be resolved when upgrading to Galaxy 20.*
				workflowName = workflowId;
			}
			workflowNames.put(workflowId, workflowName);
			log.info("Storing workflow name in local cache: " + workflowId + ": " + workflowName);
		}
		catch(Exception e) {
			// in case of exception, use workflow ID as name, but don't cache it
			workflowName = workflowId;
			log.error("Failed to get name for workflow " + workflowId + " from Galaxy.");
		}
		
		return workflowName;
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.clearWorkflowNamesCache()
	 */
	public void clearWorkflowNamesCache() {
		workflowNames.clear();
		log.info("Workflow names cache has been cleared up.");
	}
	
}
