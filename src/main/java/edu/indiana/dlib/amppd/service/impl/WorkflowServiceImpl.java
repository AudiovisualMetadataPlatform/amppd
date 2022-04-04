package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.ToolsClient;
import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowStepDefinition;
import com.sun.jersey.api.client.UniformInterfaceException;

import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of WorkflowService.
 * @author yingfeng
 */
@Service
@Slf4j
public class WorkflowServiceImpl implements WorkflowService {
	
	// tag for published workflow
	public static String PUBLISHED = "published";
	
	@Autowired
	private GalaxyApiService galaxyApiService;
	
	@Getter
	private WorkflowsClient workflowsClient;
	
	@Getter
	private ToolsClient toolsClient;
	
	// use hashmap to cache workflow names to avoid frequent query request to Galaxy in cases such as refreshing workflow results
	private Map<String, String> workflowNames = new HashMap<String, String>();
			
	/**
	 * Initialize the WorkflowServiceImpl bean.
	 */
	@PostConstruct
	public void init() {
		workflowsClient = galaxyApiService.getGalaxyInstance().getWorkflowsClient();
		toolsClient = galaxyApiService.getGalaxyInstance().getToolsClient();
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.hasWorkflowTag(Workflow, String)
	 */
	@Override
	public Boolean hasWorkflowTag(Workflow workflow, String tag) {
		for (String wtag : workflow.getTags()) {
			if (StringUtils.equalsIgnoreCase(wtag, tag)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.isWorkflowPublished(Workflow)
	 */
	@Override
	public Boolean isWorkflowPublished(Workflow workflow) {
		return workflow.isPublished() || hasWorkflowTag(workflow, PUBLISHED);
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.getWorkflows(Boolean, Boolean, Boolean)
	 */	
	@Override
	public List<Workflow> listWorkflows(Boolean showPublished, Boolean showHidden, Boolean showDeleted) {
		// TODO 
		// Below is a temporary work-around to address the Galaxy bug in get_workflows_list.
		// We can replace it with the commented code at the end of the method once the Galaxy bug is fixed;
		// provided that special care is taken to handle the case when the published tag is used.
			
		List <Workflow> workflows = workflowsClient.getWorkflows(null, showHidden, showDeleted, null);
		List <Workflow> filterWorkflows = new ArrayList <Workflow>();

		// if showPublished not specified, include both published and unpublished workflows
		if (showPublished == null ) {
			filterWorkflows = workflows;
		}
		// otherwise filter workflows based on showPublished
		else {
			for (Workflow workflow : workflows) {
				Boolean isPublished = isWorkflowPublished(workflow);
				if (showPublished && isPublished) {
					filterWorkflows.add(workflow);			
				}
				else if (!showPublished && !isPublished) {
					filterWorkflows.add(workflow);			
				}
			}
		}
		
		String published = showPublished == null ? "" : (showPublished ? "published " : "unpublished "); 
		String hidden = showHidden != null && showHidden ? "hidden " : ""; 
		String deleted = showDeleted != null && showDeleted ? "deleted " : ""; 
		log.info("Successfully listed " + filterWorkflows.size() + " " + published + hidden + deleted + "workflows currently existing in Galaxy.");
		return filterWorkflows;			
		
//		return workflowsClient.getWorkflows(showPublished, showHidden, showDeleted, null);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.showWorkflow(String, Boolean, Boolean)
	 */	
	@Override
	public WorkflowDetails showWorkflow(String workflowId, Boolean instance, Boolean includeToolName) {
		// by default, not for instance
		if (instance == null) {
			instance = false;
		}
		
		// retrieve workflow details by workflow ID from galaxy
		WorkflowDetails workflowDetails = null;
		if (instance) {
			workflowDetails = workflowsClient.showWorkflowInstance(workflowId);
		}
		else {
			workflowDetails = workflowsClient.showWorkflow(workflowId);
		}
		
		// by default, include tool name
		if (includeToolName == null) {
			includeToolName = true;
		}		

		// retrieve tool name by tool ID for each tool in the workflow steps
		if (includeToolName && workflowDetails != null) {
			Collection<WorkflowStepDefinition> steps = workflowDetails.getSteps().values();
			for (WorkflowStepDefinition step : steps) {
				String toolId = step.getToolId();
				if (!StringUtils.isEmpty(toolId)) {
					Tool tool = toolsClient.showTool(toolId);					
					if (tool != null) {
						String toolName = tool.getName();
						// use tool name if not empty, otherwise use tool ID as name
						if (!StringUtils.isEmpty(toolName)) {
							step.setToolName(toolName);
						}
						else {
							step.setToolName(toolId);
						}					
					}
				}
			}
		}
		
		String store = instance ? "" : "stored ";
		String include = includeToolName ? " with" : " without";
		log.info("Successfully retrieved workflow with " + store + "ID " + workflowId + include + " tool name.");
		return workflowDetails;
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
			/* Note: 
			 * It appears that when calling showWorkflowInstance, i.e. getting Workflow with supposedly storedWorkflowId
			 * (by setting request param instance=true), Galaxy either throws UniformInterfaceException (if the storedWorkflowId 
			 * does not exist), or return another workflow whose ID is totally different from the storedWorkflowId.
			 * In the past when querying workflow, we relied on the workflow ID returned from invocation query, which tends to be
			 * different from the real workflow ID, so we used showWorkflowInstance considering that ID was the storedWorkflowId.
			 * That's not the case anymore: we now use the workflow ID returned from workflow index, which is always the real ID.
			 * Thus, we should use showWorkflow instead of showWorkflowInstance, or we'd get wrong workflow or exception.
			 */
//			WorkflowDetails workflow = workflowsClient.showWorkflowInstance(workflowId);
			
			WorkflowDetails workflow = workflowsClient.showWorkflow(workflowId);
			if (workflow != null) {
				workflowName = workflow.getName();
			}
			else {
				// if the workflow can't be found in Galaxy, use its ID as name as a temporary solution.
				workflowName = workflowId;
				log.warn("Can't find workflow " + workflowId + " in Galaxy; will use the ID as its name");
			}
		}
		catch(UniformInterfaceException e) {
			// when Galaxy can't find the workflow by the given ID, it throws exception (instead of returning null);
			// this is likely because the ID is not a StoredWorkflow ID; in this case use workflow ID as name
			workflowName = workflowId;
			log.warn("Can't find workflow " + workflowId + " in Galaxy; will use the ID as its name\n" + e.getMessage());
		}
		
		workflowNames.put(workflowId, workflowName);
		log.info("Storing workflow name in local cache: " + workflowId + ": " + workflowName);
		return workflowName;
	}		
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.clearWorkflowNamesCache()
	 */
	public void clearWorkflowNamesCache() {
		workflowNames.clear();
		log.info("Workflow names cache has been cleared up.");
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.workflowNamesCacheSize()
	 */
	public Integer workflowNamesCacheSize() {
		return workflowNames.size();
	}
	
}
