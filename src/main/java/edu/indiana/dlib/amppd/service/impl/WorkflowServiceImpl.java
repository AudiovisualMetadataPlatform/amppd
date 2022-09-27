package edu.indiana.dlib.amppd.service.impl;

import com.github.jmchilton.blend4j.galaxy.ToolsClient;
import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowStepDefinition;
import com.sun.jersey.api.client.UniformInterfaceException;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import edu.indiana.dlib.amppd.web.WorkflowFilterValues;
import edu.indiana.dlib.amppd.web.WorkflowResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

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
	private List<Workflow> workflowCache = new ArrayList<Workflow>();
			
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
	public WorkflowResponse listWorkflows(Boolean showPublished, Boolean showHidden, Boolean showDeleted,
										  String[] tag, String[] name, String[] annotations, String[] creators, Date[] dateRange) {
		// TODO 
		// Below is a temporary work-around to address the Galaxy bug in get_workflows_list.
		// We can replace it with the commented code at the end of the method once the Galaxy bug is fixed;
		// provided that special care is taken to handle the case when the published tag is used.
		WorkflowResponse response = new WorkflowResponse();
		if(showDeleted == null && showHidden == null && showPublished == null &&
				(tag == null || tag.length <= 0) &&
				(name == null || name.length <= 0) &&
				(annotations == null || annotations.length <= 0) &&
				(creators == null || creators.length <= 0) &&
				(dateRange == null  || dateRange.length <= 0)) {
			clearWorkflowsCache();
		}

		if (workflowCache.size() <= 0) {
			workflowCache = workflowsClient.getWorkflows(null, showHidden, showDeleted, null);
		}
		List <Workflow> filterWorkflows = filters(workflowCache, showPublished, tag, name, annotations, creators, dateRange);
		WorkflowFilterValues filterBy = prepareFilters(filterWorkflows);
		String published = showPublished == null ? "" : (showPublished ? "published " : "unpublished ");
		String hidden = showHidden != null && showHidden ? "hidden " : ""; 
		String deleted = showDeleted != null && showDeleted ? "deleted " : ""; 
		log.info("Successfully listed " + filterWorkflows.size() + " " + published + hidden + deleted + "workflows currently existing in Galaxy.");
		response.setRows(filterWorkflows);
		response.setTotalResults(filterWorkflows.size());
		response.setFilters(filterBy);
		return response;
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

	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.clearWorkflowsCache()
	 */
	public void clearWorkflowsCache() {
		workflowCache.clear();
		log.info("Workflows cache has been cleared up.");
	}

	private List <Workflow> filterTags(List<Workflow> workflows, String[] tags) {
		List <Workflow> filterWorkflows = new ArrayList <Workflow>();
		for (Workflow wf : workflows) {
			for (String t : tags) {
				System.out.println("tag---->" + t);
				if (hasWorkflowTag(wf, t)) {
					filterWorkflows.add(wf);
					break;
				}
			}
		}
		return filterWorkflows;
	}

	private List<Workflow> filterCreators(List<Workflow> workflows, String[] creators) {
		List <Workflow> filterWorkflows = new ArrayList <Workflow>();
		for(String creator: creators) {
			for (Workflow wf : workflows) {
				log.info(wf.getCreator() + " ---- " + creator);
				if(StringUtils.equalsIgnoreCase(wf.getCreator(), creator) || StringUtils.equalsIgnoreCase(wf.getOwner(), creator)) {
					filterWorkflows.add(wf);
					break;
				}
			}
		}
		return filterWorkflows;
	}

	private List<Workflow> filterWFName(List<Workflow> workflows, String[] names) {
		List<Workflow> filterWorkflows = new ArrayList <Workflow>();
		for (String name : names) {
			for (Workflow wf : workflows) {
				System.out.println("tag---->" + name);
				if (StringUtils.equalsIgnoreCase(wf.getName(), name)) {
					filterWorkflows.add(wf);
					break;
				}
			}
		}
		return filterWorkflows;
	}

	private List<Workflow> filterDateRange(List<Workflow> workflows, Date[] dateRange) {
		List<Workflow> filterWorkflows = new ArrayList <Workflow>();
		Date startDate = dateRange[0];
		Date endDate = dateRange[1];
		for (Workflow wf : workflows) {
			Date wfDate = wf.getUpdateTime();
			if ((wfDate.after(startDate) || wfDate.equals(startDate))  && (wfDate.before(endDate) || wfDate.equals(endDate))) {
				filterWorkflows.add(wf);
			}
		}
		return filterWorkflows;
	}

	private List<Workflow> filterAnnotations(List<Workflow> workflows, String[] annotations) {
		List <Workflow> filterWorkflows = new ArrayList <Workflow>();
		for(String term: annotations) {
			for (Workflow wf : workflows) {
				for (String a : wf.getAnnotations()) {
					if (StringUtils.containsIgnoreCase(a, term)) {
						filterWorkflows.add(wf);
						break;
					}
				}
			}
		}
		return filterWorkflows;
	}

	private List<Workflow> filterPublished(List<Workflow> workflows, Boolean showPublished) {
		List <Workflow> filterWorkflows = new ArrayList <Workflow>();
		for (Workflow workflow : workflows) {
			Boolean isPublished = isWorkflowPublished(workflow);
			if (showPublished && isPublished) {
				filterWorkflows.add(workflow);
			} else if (showPublished != null && !showPublished && !isPublished) {
				filterWorkflows.add(workflow);
			}
		}
		return filterWorkflows;
	}

	private List<Workflow> filters(List<Workflow> workflows,
								   Boolean showPublished,
								   String[] tag,
								   String[] name,
								   String[] annotations,
								   String[] creators,
								   Date[] dateRange) {
		if(showPublished == null && tag == null && name == null && annotations == null && dateRange == null && creators == null) {
			return workflows;
		}
		List <Workflow> filterWorkflows = new ArrayList <Workflow>();
		List <Workflow> filterByPublished = showPublished != null ? filterPublished(workflows, showPublished) : null;
		List <Workflow> filterByTags = tag != null && tag.length >= 1 ? filterTags(workflows, tag) : null;
		List <Workflow> filterByNames = name != null && name.length >= 1 ? filterWFName(workflows, name) : null;
		List<Workflow> filterByAnnotations = annotations != null && annotations.length >= 1 ? filterAnnotations(workflows, annotations) : null;
		List <Workflow> filterByCreators = creators != null && creators.length >= 1 ? filterCreators(workflows, creators) : null;
		List <Workflow> filterByDate = dateRange != null && dateRange.length >= 1 ? filterDateRange(workflows, dateRange) : null;
		if(filterByPublished != null) {
			filterWorkflows = filterByPublished;
		}

		if(filterByTags != null) {
			filterWorkflows = filterWorkflows.size() <= 0 ? filterByTags :
					filterWorkflows.stream().filter(filterByTags::contains).collect(Collectors.toList());
		}

		if(filterByNames != null) {
			filterWorkflows = filterWorkflows.size() <= 0 ? filterByNames :
					filterWorkflows.stream().filter(filterByNames::contains).collect(Collectors.toList());
		}

		if(filterByAnnotations != null) {
			filterWorkflows = filterWorkflows.size() <= 0 ? filterByAnnotations :
					filterWorkflows.stream().filter(filterByAnnotations::contains).collect(Collectors.toList());
		}

		if(filterByCreators != null) {
			filterWorkflows = filterWorkflows.size() <= 0 ? filterByCreators :
					filterWorkflows.stream().filter(filterByCreators::contains).collect(Collectors.toList());
		}

		if(filterByDate != null) {
			filterWorkflows = filterWorkflows.size() <= 0 ? filterByDate :
					filterWorkflows.stream().filter(filterByDate::contains).collect(Collectors.toList());
		}

		return filterWorkflows;
	}

	private WorkflowFilterValues prepareFilters(List<Workflow> workflows) {
		WorkflowFilterValues filters = new WorkflowFilterValues();
		List<String> names = new ArrayList<String>();
		List<String> tags = new ArrayList<String>();
		List<String> creators = new ArrayList<String>();
		for(Workflow wf: workflows) {
			names.add(wf.getName());
			if(wf.getCreator() == null || wf.getCreator() == ""){
				creators.add(wf.getOwner());
			}else{
				creators.add(wf.getCreator());
			}
			for(String tag: wf.getTags()) {
				tags.add(tag);
			}
		}

		filters.setCreators(creators.stream().distinct().collect(Collectors.toList()));
		filters.setNames(names.stream().distinct().collect(Collectors.toList()));
		filters.setTags(tags.stream().distinct().collect(Collectors.toList()));
		return filters;
	}
}
