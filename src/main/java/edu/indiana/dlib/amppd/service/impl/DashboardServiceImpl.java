package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationDetails;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationStepDetails;
import com.github.jmchilton.blend4j.galaxy.beans.Job;
import com.github.jmchilton.blend4j.galaxy.beans.JobInputOutput;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.model.DashboardResult;
import edu.indiana.dlib.amppd.model.MgmTool;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.DashboardRepository;
import edu.indiana.dlib.amppd.repository.MgmToolRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.DashboardService;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import edu.indiana.dlib.amppd.util.CacheHelper;
import edu.indiana.dlib.amppd.web.DashboardResponse;
import edu.indiana.dlib.amppd.web.DashboardSearchQuery;
import edu.indiana.dlib.amppd.web.GalaxyJobState;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DashboardServiceImpl implements DashboardService{

	@Autowired
	private GalaxyPropertyConfig galaxyPropertyConfig;
	@Autowired
	private PrimaryfileRepository primaryfileRepository;
	@Autowired
	private MgmToolRepository mgmToolRepository;
	@Autowired
	private JobService jobService;
	@Autowired
	private WorkflowService workflowService;
	@Autowired
	private CacheHelper cache;
	@Autowired
	private DashboardRepository dashboardRepository;
	
	@Value("${amppd.refreshDashboardMinutes}")
	private int REFRESH_MINUTES;
	private String CACHE_KEY ="DashboardResults";
	
	
	/**
	 * Return true if the specified dateRefreshed is recent (i.e. within a predefined period); false otherwise.
	 */
	private boolean isDateRefreshedRecent(Date dateRefreshed) {		
		// TODO This method is used by both refreshing the result status and refreshing the whole table,
		// for the former, the refresh rate is based on how long a Galaxy job takes to complete;
		// for the latter, the refresh rate is based on how long it takes to refresh the whole table;
		// if the time differs a lot we may need to define the thresholds separately
		return dateRefreshed != null && dateRefreshed.compareTo(DateUtils.addMinutes(new Date(), -REFRESH_MINUTES)) > 0;
	}
		
	/**
	 * Return true if we should refresh the status of the specified DashboardResult from job status in galaxy.
	 * A DashboardResult needs refresh if it's existing status could still change (i.e. not COMPLETE or ERROR)
	 * and its last refreshed timestamp is older than the refresh rate threshold.
	 */
	private boolean shouldRefreshResultStatus(DashboardResult result) {
		return !isDateRefreshedRecent(result.getDateRefreshed()) && result.getStatus() != GalaxyJobState.COMPLETE && result.getStatus() != GalaxyJobState.ERROR;
	}
	
	/**
	 * Refresh the status of the specified DashboardResult from job status in galaxy.
	 */
	private DashboardResult refreshResultStatus(DashboardResult result) {
		try {
			Dataset ds = jobService.showJobStepOutput(result.getWorkflowId(), result.getInvocationId(), result.getStepId(), result.getOutputId());
			String state = ds.getState();
			GalaxyJobState status = getJobStatus(state);
			result.setStatus(status);
		}
		catch(Exception ex) {
			log.info("Unable to update the status of invocation " + result.getInvocationId() + " from Galaxy");
		}
		
		result.setDateRefreshed(new Date());		
		dashboardRepository.save(result);		
		return result;		
	}
			
	/**
	 * Refresh status of the specified dashboardResults as needed by retrieving corresponding job status from Galaxy.
	 */
	private List<DashboardResult> refreshResultsStatusAsNeeded(List<DashboardResult> dashboardResults) {
		for(DashboardResult result : dashboardResults) {
			if(shouldRefreshResultStatus(result)) {
				result = refreshResultStatus(result);
			}
		}
		return dashboardResults;
	}
		
	/**
	 * @see edu.indiana.dlib.amppd.service.DashboardService.getDashboardResults(DashboardSearchQuery)
	 */
	public DashboardResponse getDashboardResults(DashboardSearchQuery query){
		//List<DashboardResult> results = (List<DashboardResult>) cache.get(CACHE_KEY, false);		
		//if(results!=null) {
		//	return results;
		//}
		
		DashboardResponse response = dashboardRepository.searchResults(query);
		refreshResultsStatusAsNeeded(response.getRows());
		return response;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DashboardService.getFinalDashboardResults(Long)
	 */
	public List<DashboardResult> getFinalDashboardResults(Long primaryfileId) {
		return refreshResultsStatusAsNeeded(dashboardRepository.findByPrimaryfileIdAndIsFinalTrue(primaryfileId));
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DashboardService.addDashboardResult(String, String, long, String)
	 */
	public void addDashboardResult(String workflowId, String workflowName, long primaryfileId, String historyId) {
		List<DashboardResult> results = new ArrayList<DashboardResult>();
		
		try {
			List<Invocation> invocations = jobService.listJobs(workflowId, primaryfileId);
			
			if(invocations.isEmpty()) return;
			
			for(Invocation invocation : invocations) {
				if(dashboardRepository.invocationExists(invocation.getId())) {
					continue;
				}
				InvocationDetails detail = (InvocationDetails)jobService.getWorkflowsClient().showInvocation(workflowId, invocation.getId(), true);
				
				// Check to see if we have an associated primary file.
				List<Primaryfile> files = primaryfileRepository.findByHistoryId(detail.getHistoryId());
				
				// If not, skip this invocation
				if(files.isEmpty()) continue;
				
				// Grab the first primary file, although should only be one.
				Primaryfile thisFile = files.get(0);
				
				// Iterate through each step.  Each of which has a list of jobs (unless it is the initial input)				
				for(InvocationStepDetails step : detail.getSteps()) {
					// If we have no jobs, don't add a result here (this is the case for the input step of a workflow)
					List<Job> jobs = step.getJobs();
					if(jobs.isEmpty()) continue;
					
					// TODO confirm what Galaxy step/job timestamps represent
					// Theoretically the timestamps of a step and its job should be the same; 
					// however only the updated timestamp of a step is available in step detail; 
					// and it differs from either the created or updated timestamp of the job;
					// for now we use the created/updated timestamp of the job in the result
					Date dateCreated = step.getUpdateTime(); // will be overwritten below
					Date dateUpdated = step.getUpdateTime(); // will be overwritten below
					
					// It's possible to have more than one job per step, although we don't have any examples at the moment
					GalaxyJobState status = GalaxyJobState.UNKNOWN;
					String jobName = "";
					String toolInfo = "";
					for(Job job : jobs) {
						// Concatenate the job names and tool info in case we have more than one. 
						jobName = jobName + job.getToolId() + " ";
						dateCreated = job.getCreated();
				 		dateUpdated = job.getUpdated();
						status = getJobStatus(job.getState());
						String tinfo = getMgmToolInfo(job.getToolId(), dateCreated);
						String divider = toolInfo == "" ? "" : ", ";
						toolInfo += tinfo == null ? "" : divider + tinfo;
					}

					// For each output, create a record.
					Map<String, JobInputOutput> outputs = step.getOutputs();
					for(String key : outputs.keySet()) {
						JobInputOutput output = outputs.get(key);
						Dataset dataset = jobService.showJobStepOutput(detail.getWorkflowId(), detail.getId(), step.getId(), output.getId());
						
						// Show only relevant output
						if(dataset!=null && !dataset.getVisible()) continue;
						
						DashboardResult result = new DashboardResult();
						result.setHistoryId(historyId);
						result.setWorkflowId(workflowId);
						result.setOutputId(output.getId());
						result.setWorkflowStep(jobName);
						result.setSubmitter(galaxyPropertyConfig.getUsername());
						result.setDateCreated(dateCreated);
						result.setDateUpdated(dateUpdated);
						result.setStatus(status);
						result.setWorkflowName(workflowName);
						result.setInvocationId(invocation.getId());
						result.setStepId(step.getId());
						
						result.setPrimaryfileId(thisFile.getId());
						result.setSourceFilename(thisFile.getName());
						result.setSourceItem(thisFile.getItem().getName());
												
						result.setOutputFile(key);
						result.setOutputType(dataset.getFileExt());
						result.setOutputPath(dataset.getFileName());
						result.setToolInfo(toolInfo);
						
						result.setDateRefreshed(new Date());
						results.add(result);
					}
				}
					
			}
			
			dashboardRepository.saveAll(results);
			// Expunge the cache
			cache.remove(CACHE_KEY);
		}
		catch(Exception ex) {
			log.error("Error getting dashboard results", ex);
		}
	}
	
	/**
	 * 
	 */
	public List<DashboardResult> refreshDashboardResults() {				
		List<DashboardResult> allResults = new ArrayList<DashboardResult>();
		List<Primaryfile> primaryfiles = primaryfileRepository.findByHistoryIdNotNull();

		// process Galaxy invocation details per primaryfile instead of retrieving all, in order to avoid timeout issue in Galaxy
		for (Primaryfile primaryfile : primaryfiles) {
			// skip the primaryfile if all of its results have been recently refreshed;
			// this allows rerun of the refresh to continue with unfinished primaryfiles in case of a failure
			Date oldestDateRefreshed = this.dashboardRepository.findOldestDateRefreshedByPrimaryfileId(primaryfile.getId());
			if (isDateRefreshedRecent(oldestDateRefreshed)) continue;
			
			List<InvocationDetails> invocations = jobService.getWorkflowsClient().indexInvocationsDetails(galaxyPropertyConfig.getUsername(), null, primaryfile.getHistoryId());
			for (InvocationDetails invocation : invocations) {
				List<DashboardResult> results = refreshDashboardResults(invocation, null, primaryfile);
				allResults.addAll(results);
			}
		}
				
		return allResults;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DashboardService.refreshAllDashboardResults()
	 */
	public List<DashboardResult> refreshAllDashboardResults(){
		List<DashboardResult> results = new ArrayList<DashboardResult>();
		try {
			// Get a list of invocation details from Galaxy
			List<InvocationDetails> details = jobService.getWorkflowsClient().indexInvocationsDetails(galaxyPropertyConfig.getUsername());
			
			// For each detail
			for(InvocationDetails detail : details) {
				if(dashboardRepository.invocationExists(detail.getId())) {
					continue;
				}
				// Check to see if we have an associated primary file.
				List<Primaryfile> files = primaryfileRepository.findByHistoryId(detail.getHistoryId());
				
				// If not, skip this invocation
				if(files.isEmpty()) continue;
				
				// Grab the first primary file, although should only be one.
				Primaryfile thisFile = files.get(0);
				
				// Iterate through each step.  Each of which has a list of jobs (unless it is the initial input)				
				for(InvocationStepDetails step : detail.getSteps()) {
					// If we have no jobs, don't add a result here
					List<Job> jobs = step.getJobs();
					if(jobs.isEmpty()) continue;
					
					String workflowName = detail.getWorkflowId();
					Map<String, String> workflowDetails = new HashMap<String, String>();
					if(workflowDetails.containsKey(detail.getWorkflowId())) {
						workflowName = workflowDetails.get(detail.getWorkflowId());
					}
					else {
						try {
							WorkflowDetails workflow = workflowService.getWorkflowsClient().showWorkflowInstance(detail.getWorkflowId());
							if(workflow!=null) {
								workflowName = workflow.getName();
							}
						}
						catch(Exception ex) {
							String msg = "Unable to retrieve workflows from Galaxy.";
							log.error(msg);
						}
						workflowDetails.put(detail.getWorkflowId(), workflowName);
					}
					
					// TODO confirm what Galaxy step/job timestamps represent
					// Theoretically the timestamps of a step and its job should be the same; 
					// however only the updated timestamp of a step is available in step detail; 
					// and it differs from either the created or updated timestamp of the job;
					// for now we use the created/updated timestamp of the job in the result
					Date dateCreated = step.getUpdateTime(); // will be overwritten below
					Date dateUpdated = step.getUpdateTime(); // will be overwritten below
					
					// It's possible to have more than one job per step, although we don't have any examples at the moment
					GalaxyJobState status = GalaxyJobState.UNKNOWN;
					String jobName = "";
					String toolInfo = "";
					for(Job job : jobs) {
						// Concatenate the job names and tool info in case we have more than one. 
						jobName = jobName + job.getToolId() + " ";
						dateCreated = job.getCreated();
				 		dateUpdated = job.getUpdated();
						status = getJobStatus(job.getState());
						String tinfo = getMgmToolInfo(job.getToolId(), dateCreated);
						String divider = toolInfo == "" ? "" : ", ";
						toolInfo += tinfo == null ? "" : divider + tinfo;
					}

					// For each output, create a record.
					Map<String, JobInputOutput> outputs = step.getOutputs();
					for(String key : outputs.keySet()) {
						JobInputOutput output = outputs.get(key);
						Dataset dataset = jobService.showJobStepOutput(detail.getWorkflowId(), detail.getId(), step.getId(), output.getId());
						
						// Show only relevant output
						if(dataset!=null && !dataset.getVisible()) continue;
						
						DashboardResult result = new DashboardResult();
						result.setHistoryId(detail.getHistoryId());
						result.setWorkflowId(detail.getWorkflowId());
						result.setOutputId(output.getId());
						result.setWorkflowStep(jobName);
						result.setSubmitter(galaxyPropertyConfig.getUsername());
						result.setDateCreated(dateCreated);
						result.setDateUpdated(dateUpdated);
						result.setStatus(status);
						result.setWorkflowName(workflowName);
						result.setInvocationId(detail.getId());
						result.setStepId(step.getId());
						
						result.setPrimaryfileId(thisFile.getId());
						result.setSourceFilename(thisFile.getName());
						result.setSourceItem(thisFile.getItem().getName());
												
						result.setOutputFile(key);
						result.setOutputType(dataset.getFileExt());
						result.setOutputPath(dataset.getFileName());
						result.setToolInfo(toolInfo);
						
						result.setDateRefreshed(new Date());
						results.add(result);
						
					}
				}

				dashboardRepository.saveAll(results);
			}
		}
		catch(Exception ex) {
			log.error("Error getting dashboard results", ex);
		}
		cache.put(CACHE_KEY, results, REFRESH_MINUTES * 60);
		return results;
	}
	
	/**
	 * 
	 */
	private List<DashboardResult> refreshDashboardResults(InvocationDetails invocation, Workflow workflow, Primaryfile primaryfile) {
		List<DashboardResult> results = new ArrayList<DashboardResult>();
		
		// if the passed-in primaryfile is null, get primaryfile info by its ID from the passed-in invocation
		if (primaryfile == null) {
			// Check to see if we have an associated primary file.
			List<Primaryfile> files = primaryfileRepository.findByHistoryId(invocation.getHistoryId());
			
			// If not, skip this invocation as it is not invoked by AMP
			if (files.isEmpty()) return results;
			
			// Grab the first primary file, although should only be one.
			primaryfile = files.get(0);	
		}

		// get workflow name either from the passed-in workflow, or retrieve it by its ID from the passed-in invocation 
		String workflowName = workflow != null ? workflow.getName() : workflowService.getWorkflowName(invocation.getWorkflowId());
		
		
		// Iterate through each step, each of which has a list of jobs (unless it is the initial input)				
		for(InvocationStepDetails step : invocation.getSteps()) {
			// If we have no jobs, don't add a result here
			List<Job> jobs = step.getJobs();
			if (jobs.isEmpty()) continue;
						
			// TODO confirm what Galaxy step/job timestamps represent
			// Theoretically the timestamps of a step and its job should be the same; 
			// however only the updated timestamp of a step is available in step invocation; 
			// and it differs from either the created or updated timestamp of the job;
			// for now we use the created/updated timestamp of the job in the result
			Date dateCreated = step.getUpdateTime(); // will be overwritten below
			Date dateUpdated = step.getUpdateTime(); // will be overwritten below
			
			// It's possible to have more than one job per step, although we don't have any examples at the moment
			GalaxyJobState status = GalaxyJobState.UNKNOWN;
			String stepLabel = "";
			String toolInfo = "";
			for (Job job : jobs) {
				// Concatenate the job names and tool info in case we have more than one. 
				stepLabel = stepLabel + job.getToolId() + " ";
				dateCreated = job.getCreated();
		 		dateUpdated = job.getUpdated();
				status = getJobStatus(job.getState());
				String tinfo = getMgmToolInfo(job.getToolId(), dateCreated);
				String divider = toolInfo == "" ? "" : ", ";
				toolInfo += tinfo == null ? "" : divider + tinfo;
			}

			// For each output, create a result record.
			Map<String, JobInputOutput> outputs = step.getOutputs();
			for (String outputName : outputs.keySet()) {
				JobInputOutput output = outputs.get(outputName);
				Dataset dataset = jobService.showJobStepOutput(invocation.getWorkflowId(), invocation.getId(), step.getId(), output.getId());
				
				// Show only relevant output
				if (dataset == null || !dataset.getVisible()) continue;
				
				// initialize result as not final
				DashboardResult result = new DashboardResult(); 
				result.setIsFinal(false);

				// retrieve the result for this output if already existing in the workflow result table,
				// so we can preserve the isFinal field in case it has been set; also, this allows update of existing records, 
				// otherwise we have to delete all rows before adding refreshed results in order to avoid redundancy
				List<DashboardResult> oldResults = dashboardRepository.findByOutputId(output.getId());				
				if (oldResults != null) {
					// oldresults is unique throughout Galaxy, so there should only be one result per output
					result = oldResults.get(0);
					
					// if there are more than one result then there must have been some DB inconsistency
					// scan the results to see if any is final, if so use that one, and delete all others
					if (oldResults.size() > 1) {
						log.warn("Found " + oldResults.size() + " workflow results for output: " + output.getId());						
						for (DashboardResult oldResult : oldResults) {
							if (oldResult.getIsFinal() && !result.getIsFinal()) {
								dashboardRepository.delete(result);
								result = oldResult;
							}
							else {
								dashboardRepository.delete(oldResult);
							}
						}
					}
				}
								
				result.setPrimaryfileId(primaryfile.getId());
				result.setSourceFilename(primaryfile.getName());
				result.setSourceItem(primaryfile.getItem().getName());
										
				result.setWorkflowId(invocation.getWorkflowId());
				result.setInvocationId(invocation.getId());
				result.setStepId(step.getId());
				result.setOutputId(output.getId());
				result.setHistoryId(invocation.getHistoryId());

				result.setWorkflowName(workflowName);
				result.setWorkflowStep(stepLabel);
				result.setToolInfo(toolInfo);

				result.setOutputFile(outputName);
				result.setOutputType(dataset.getFileExt());
				result.setOutputPath(dataset.getFileName());
				// outputLink ia set later when output is first accessed on dashboard

				result.setSubmitter(galaxyPropertyConfig.getUsername());
				result.setStatus(status);
				result.setDateCreated(dateCreated);
				result.setDateUpdated(dateUpdated);
								
				result.setDateRefreshed(new Date());
				results.add(result);				
			}
		}

		return results;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.DashboardService.setResultIsFinal(long, boolean)
	 */
	public boolean setResultIsFinal(long dashboardResultId, boolean isFinal) {
		
		Optional<DashboardResult> dashboardResultOpt  = dashboardRepository.findById(dashboardResultId);
		
		if(dashboardResultOpt.isPresent()) {
			DashboardResult result = dashboardResultOpt.get();
			result.setIsFinal(isFinal);
			dashboardRepository.save(result);
			
			return true;
		}
		return false;
	}
		
	// Map the status in Galaxy to what we want on the front end.
	private GalaxyJobState getJobStatus(String jobStatus) {
		GalaxyJobState status = GalaxyJobState.UNKNOWN;
		if(jobStatus.equals("ok")) {
			status = GalaxyJobState.COMPLETE;
		}
		else if(jobStatus.equals("running")) {
			status = GalaxyJobState.IN_PROGRESS;
		}
		else if(jobStatus.equals("scheduled")||jobStatus.equals("new")||jobStatus.equals("queued")) {
			status = GalaxyJobState.SCHEDULED;
		}
		else if(jobStatus.equals("error")) {
			status = GalaxyJobState.ERROR;
		}
		else if(jobStatus.equals("paused")) {
			status = GalaxyJobState.PAUSED;
		}
		return status;
	}
	
	/*
	 * Get the latest model/version of the specified MGM tool at the point when the Galaxy job was run.
	 * @param toolId Galaxy tool ID for the MGM tool
	 * @param invocationTime the invocation time of the Galaxy job
	 * @return the latest tool information found or null if not found
	 */
	private String getMgmToolInfo(String toolId, Date invocationTime) {
		List<MgmTool> tools = mgmToolRepository.findLatestByToolId(toolId, invocationTime);
		if (tools == null || tools.size() == 0)
			return null;
		String info = tools.get(0).getMgmName() + " " + tools.get(0).getVersion();
		return info;
	}
	
}
