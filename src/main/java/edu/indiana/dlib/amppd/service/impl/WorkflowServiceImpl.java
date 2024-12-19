package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.ToolsClient;
import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputDefinition;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowMetadata;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowStepDefinition;
import com.sun.jersey.api.client.UniformInterfaceException;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.model.MgmTool;
import edu.indiana.dlib.amppd.repository.MgmToolRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import edu.indiana.dlib.amppd.web.WorkflowFilterValues;
import edu.indiana.dlib.amppd.web.WorkflowResponse;
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
	private WorkflowResultRepository workflowResultRepository;	

	@Autowired
	private MgmToolRepository mgmToolRepository;	

	@Autowired
	private GalaxyApiService galaxyApiService;

	@Getter
	private WorkflowsClient workflowsClient;

	@Getter
	private ToolsClient toolsClient;

	// use hashmap to cache workflow names to avoid frequent query request to Galaxy in cases such as refreshing workflow results
	private Map<String, String> workflowNames = new HashMap<String, String>();

	// use following list to cache result of workflow for listing and once it is loaded we use it for filtering
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
		WorkflowResponse response = new WorkflowResponse();

		/* Note:
		 * It appears that workflowCache was introduced when workflow search filters were added. 
		 * The purpose was to avoid redundant Galaxy API calls each time a search is initiated by AMP UI.
		 * Because Galaxy API call to list workflows doesn't handle extra filters used in AMP, 
		 * these filters are handled by AMP; thus, it make sense to not call Galaxy again once 
		 * the initial retrieval of workflows with Galaxy criteria is done and saved in cache.
		 * Below code assumes when AMP filters exist in the request, the initial Galaxy call would have occurred.
		 * This is true only if AMP UI always list all workflows for Galaxy-only filters before doing search.
		 * So far this is how AMP UI works: users would need to go to Workflow List page first, 
		 * which list all workflows, before they can do any search on workflows.  
		 * However, if UI changes in the future, below code will need update.
		 * Also, note that the Galaxy filters should not be included in below condition for clearing cache,
		 * because there're use cases where workflows are listed outside of the Workflow List page,
		 * i.e. on Run Workflow page, where showPublished is not null and the cache should be cleared.
		 * TODO:
		 * In case AMP UI does change in the future, and above assumption won't hold true, 
		 * a better way to maintain the workflowCache might be to clear it anytime any workflow is changed, i.e. 
		 * created, edited, deleted, which can be achieved by calling clearWorkflowsCache in WorkflowEditProxy APIs.
		 * However, even this approach won't detect workflow changes made directly in Galaxy.
		 */
		// if all filtering values are null then we will clear cache and load new data from galaxy api call
		if ((tag == null || tag.length <= 0) &&
				(name == null || name.length <= 0) &&
				(annotations == null || annotations.length <= 0) &&
				(creators == null || creators.length <= 0) &&
				(dateRange == null  || dateRange.length <= 0)) {
			clearWorkflowsCache();
		}

		// refreshing workflow cache data
		if (workflowCache.size() <= 0) {
			workflowCache = workflowsClient.getWorkflows(null, showHidden, showDeleted, null);
			// TODO 
			// In above code  null is passed instead of showPublished,
			// as a temporary work-around to address the Galaxy bug in get_workflows_list.
			// We can replace it with the commented code below once the Galaxy bug is fixed;
			// provided that special care is taken to handle the case when the published tag is used.
//			return workflowsClient.getWorkflows(showPublished, showHidden, showDeleted, null);			
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
	public WorkflowDetails showWorkflow(String workflowId, Boolean instance, Boolean includeToolName, Boolean includeInputDetails) {
		// by default, not for instance
		if (instance == null) {
			instance = false;
		}

		// by default, include tool name
		if (includeToolName == null) {
			includeToolName = true;
		}		

		// by default, include input detail
		if (includeInputDetails == null) {
			includeInputDetails = true;
		}		

		String store = instance ? "" : "stored ";
		String withtn = includeToolName ? " with" : " without";
		String withid = includeInputDetails ? " with" : " without";

		// retrieve workflow details by workflow ID from galaxy
		WorkflowDetails workflowDetails = null;
		if (instance) {
			workflowDetails = workflowsClient.showWorkflowInstance(workflowId);
		}
		else {
			workflowDetails = workflowsClient.showWorkflow(workflowId);
		}

		if (workflowDetails == null) {
			throw new GalaxyWorkflowException("Failed to retrieve workflow with " + store + "ID " + workflowId + withtn + " tool name and " +  withid + " input details.");		
		}

		// retrieve tool name by tool ID for each tool in the workflow steps
		if (includeToolName) {
			populateToolNames(workflowDetails);
		}

		// populate input details such as data type, and whether the input is a primaryfile or an intermediate workflow result
		if (includeInputDetails) {
			populateInputDetails(workflowDetails);
		}

		log.info("Successfully retrieved workflow with " + store + "ID " + workflowId + withtn + " tool name, " +  withid + " input details ");
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
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.updateWorkflow(String, Boolean, Boolean)
	 */
	public WorkflowDetails updateWorkflow(String workflowId, Boolean publish, Boolean activate) {
		// check if the workflow is involved in any on-going invocation
		// if yes, throw GalaxyWorkflowException to inform caller of the method
		Boolean processing = workflowResultRepository.existsByWorkflowIdAndStatusIn(workflowId, WorkflowResultServiceImpl.PROCESSING_STATUSES);	
		if (processing) {
			throw new GalaxyWorkflowException("Workflow " + workflowId + " can't be deactivated as it is involved in some on-going invocations.");
		}
		
		// otherwise, update workflow
		WorkflowMetadata wfmd = new WorkflowMetadata();
		if (publish != null) {
			wfmd.setPublished(publish);
		}
		if (activate != null) {
			wfmd.setHidden(!activate);
		}
		WorkflowDetails workflow = workflowsClient.updateWorkflow(workflowId, wfmd);
		
		// clear workflow list cache since the list might need refresh due to this update
		clearWorkflowsCache();		
		
		log.info("Successfully updated workflow " + workflowId + ", publish: " + publish + ", activate: " + activate);		
		return workflow;
	}

	protected void clearWorkflowsCache() {
		workflowCache.clear();
		log.info("Workflows cache has been cleared up.");
	}

	/**
	 * Returns true if the input of the given format should be fed from a primaryfile.  
	 */
	protected boolean fromPrimaryfile(String format) {
		// we don't require format to be specified for primaryfile input, so null format indicates primaryfile
		return StringUtils.isBlank(format) || "av".equals(format) || "audio".equals(format) || "video".equals(format);
	}

	/**
	 * Return true if the given workflowDetails is a partial one; 
	 * false otherwise, i.e. there is one and only one primaryfile input.
	 */
	protected boolean isPartial(WorkflowDetails workflowDetails) {
		int idx = workflowDetails.getInputPrimaryfileIndex();
		int n = workflowDetails.getInputWprkflowResultLabels().size();
		// if the workflow has no input, it's invalid for submission, the error will be logged but no exception shall be thrown,
		// because this method is called when just displaying a workflow, not actually submitting yet. 
		if (n == 0 && idx < 0) {
			log.warn("Invalid workflow " + workflowDetails.getId() + " for submission: has no valid input!");
			return false;
			//			throw new GalaxyWorkflowException("Invalid workflow " + workflowDetails.getId() + ": has no valid input!"); 
		}
		return !(n == 0 && idx == 0);
	}

	/**
	 * Populate input details for the specified workflowDetails retrieved from Galaxy.
	 */
	private void populateInputDetails(WorkflowDetails workflowDetails) {
		// initialize input details fields
		List<String> labels = new ArrayList<String>();
		List<String> formats = new ArrayList<String>();
		workflowDetails.setInputWprkflowResultLabels(labels);
		workflowDetails.setInputWprkflowResultFormats(formats);
		workflowDetails.setInputPrimaryfileIndex(-1); // -1 indicate no primaryfile input
		workflowDetails.setInputPrimaryfileFormat("");
		int n = workflowDetails.getInputs().size();

		// iterate through all input nodes, indices of these steps range from 0 to (n-1) 
		for (Integer i=0; i<n; i++) {
			String stepMsg = "Invalid workflow " + workflowDetails.getId() + ": step " + i;
			String inputMsg = "Invalid workflow " + workflowDetails.getId() + ": input " + i;

			// verify that the step corresponding to the input index exists
			WorkflowStepDefinition step = workflowDetails.getSteps().get(i.toString());
			if (step == null) {
				throw new GalaxyWorkflowException(stepMsg + " doesn't exist!");
			}

			// verify that the step is an input node
			String type = (String)step.getType();
			if (!"data_input".equals(type)) {
				throw new GalaxyWorkflowException(stepMsg  + " is not an input node!");
			}

			// for some reason, input format is returned sometimes as a String, sometimes as an array list with one String element
			Object formatobj = step.getToolInputs().get("format");
			String format = null;
			if (formatobj != null) {
				format = formatobj instanceof String ? (String)formatobj : ((ArrayList<String>)formatobj).get(0);
			}

			// set index/format for Pfile input
			if (fromPrimaryfile(format)) {
				int idx = workflowDetails.getInputPrimaryfileIndex();
				if (idx >= 0) {
					throw new GalaxyWorkflowException(stepMsg + " of format " + format + " should be the only input from prirmayfile, but there is already one such step " + idx + " with format " + workflowDetails.getInputPrimaryfileFormat());
				}
				workflowDetails.setInputPrimaryfileIndex(i);
				workflowDetails.setInputPrimaryfileFormat(format);
			}
			// set label/format for result input
			else {
				// verify that the step corresponding to the input index exists
				WorkflowInputDefinition input = workflowDetails.getInputs().get(i.toString());
				if (input == null) {
					throw new GalaxyWorkflowException(inputMsg + " doesn't exist!");
				}				
				labels.add(input.getLabel());
				formats.add(format);
			}			
		}

		// decide if workflow is partial
		workflowDetails.setPartial(isPartial(workflowDetails));
	}

	/**
	 * Populate tool names for the specified workflowDetails retrieved from Galaxy.
	 */
	private void populateToolNames(WorkflowDetails workflowDetails ) {
		Collection<WorkflowStepDefinition> steps = workflowDetails.getSteps().values();

		// populate tool name for each step
		for (WorkflowStepDefinition step : steps) {
			String toolId = step.getToolId();

			// skip the input nodes/steps for which toolId is null 
			if (StringUtils.isEmpty(toolId)) continue;

			// it's more efficient to get tool name from MgmTool table than querying Galaxy
			MgmTool mgm = mgmToolRepository.findFirstByToolId(toolId);	

			// most likely, the MGM should exist in AMP table, use its name then
			if (mgm != null) {
				step.setToolName(mgm.getName());
			}
			// otherwise, retrieve it from Galaxy as a safeguard
			else {
				log.warn("Couldn't find MGM with toolId " + toolId + " in MgmTool table, retrieving from Galaxy instead ...");	

				// note that Galaxy throws exception if tool not found by ID, instead of returning null
				try {
					Tool tool = toolsClient.showTool(toolId);	
					step.setToolName(tool.getName());
				} catch (Exception e) {
					throw new RuntimeException("Couldn't find MGM with toolId " + toolId + " in Galaxy ");										
				}
			}
		}		
	}

	private Boolean filterTags(Workflow workflow, String[] tags) {
		if (tags == null || tags.length <= 0) {
			return true;
		}
		for (String t : tags) {
			if (hasWorkflowTag(workflow, t)) {
				return true;
			}
		}
		return false;
	}

	private Boolean filterCreators(Workflow workflow, String[] creators) {
		if(creators == null || creators.length <= 0) {
			return true;
		}
		for(String creator: creators) {
			if(StringUtils.equalsIgnoreCase(workflow.getCreator(), creator) || StringUtils.equalsIgnoreCase(workflow.getOwner(), creator)) {
				return true;
			}
		}
		return false;
	}

	private Boolean filterWFName(Workflow workflow, String[] names) {
		if (names == null || names.length <= 0) {
			return true;
		}
		for (String name : names) {
			if (StringUtils.equalsIgnoreCase(workflow.getName(), name)) {
				return true;
			}
		}
		return false;
	}

	private Boolean filterDateRange(Workflow workflow, Date[] dateRange) {
		if(dateRange == null || dateRange.length <= 0) {
			return true;
		}
		Date startDate = dateRange[0];
		Date endDate = dateRange[1];
		Date wfDate = workflow.getUpdateTime();
		if ((wfDate.after(startDate) || wfDate.equals(startDate))  && (wfDate.before(endDate) || wfDate.equals(endDate))) {
			return true;
		}
		return false;
	}

	private Boolean filterAnnotations(Workflow workflow, String[] annotations) {
		if (annotations == null || annotations.length <= 0) {
			return true;
		}
		for(String term: annotations) {
			for (String a : workflow.getAnnotations()) {
				if (StringUtils.containsIgnoreCase(a, term)) {
					return true;
				}
			}
		}
		return false;
	}

	private Boolean filterPublished(Workflow workflow, Boolean showPublished) {
		Boolean isPublished = isWorkflowPublished(workflow);
		if (showPublished == null) {
			return true;
		}
		else if ((showPublished && isPublished) || (showPublished != null && !showPublished && !isPublished)) {
			return true;
		}
		return false;
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
		for (Workflow workflow : workflows) {
			Boolean filterByPublished = filterPublished(workflow, showPublished);
			Boolean filterByTags = filterTags(workflow, tag);
			Boolean filterByNames = filterWFName(workflow, name);
			Boolean filterByAnnotations = filterAnnotations(workflow, annotations);
			Boolean filterByCreators = filterCreators(workflow, creators);
			Boolean filterByDate = filterDateRange(workflow, dateRange);
			if (filterByPublished && filterByTags && filterByNames && filterByAnnotations && filterByCreators && filterByDate) {
				filterWorkflows.add(workflow);
			}
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
			if(wf.getCreator() == null || wf.getCreator() == "") {
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
