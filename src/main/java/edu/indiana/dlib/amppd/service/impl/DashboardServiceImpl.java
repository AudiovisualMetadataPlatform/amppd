package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationDetails;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationStepDetails;
import com.github.jmchilton.blend4j.galaxy.beans.Job;
import com.github.jmchilton.blend4j.galaxy.beans.JobInputOutput;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.model.DashboardResult;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.DashboardRepository;
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
	 * Check to see whether or not we should get job status from galaxy
	 * @param jobState Current job state
	 * @param lastUpdated Date the database was last updated
	 * @return
	 */
	private boolean shouldRefreshJobState(GalaxyJobState jobState, Date lastUpdated) {
		if(lastUpdated.compareTo(DateUtils.addMinutes(new Date(), -REFRESH_MINUTES))>0) {
			return false;
		}
		switch(jobState) {
			case COMPLETE:
			case ERROR:
				return false;
			default:
				return true;
		}
	}
	
	/**
	 * Updates job status from galaxy
	 * @param result
	 * @return
	 */
	private DashboardResult updateDashboardResult(DashboardResult result) {
		try {
			Dataset ds = jobService.showJobStepOutput(result.getWorkflowId(), result.getInvocationId(), result.getStepId(), result.getOutputId());
			String state = ds.getState();
			
			GalaxyJobState status = getJobStatus(state);
			
			result.setStatus(status);
			
		}
		catch(Exception ex) {
			log.info("Unable to update the status of invocation " + result.getInvocationId() + " from Galaxy");
		}
		
		result.setUpdateDate(new Date());
		
		dashboardRepository.save(result);
		
		return result;		
	}
	
	/**
	 * Gets all records from the database and updates where appropriate
	 */
	public DashboardResponse getDashboardResults(DashboardSearchQuery query){
		//List<DashboardResult> results = (List<DashboardResult>) cache.get(CACHE_KEY, false);
		
		//if(results!=null) {
		//	return results;
		//}
		DashboardResponse response = dashboardRepository.searchResults(query);
		
		for(DashboardResult result : response.getRows()) {
			if(shouldRefreshJobState(result.getStatus(), result.getUpdateDate())) {
				result = updateDashboardResult(result);
			}
		}
		return response;
	}
	/**
	 * Adds a record to galaxy
	 */
	public void addDashboardResult(String workflowId, String workflowName, long primaryfileId, String historyId) 
	{
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
					// If we have no jobs, don't add a result here
					List<Job> jobs = step.getJobs();
					if(jobs.isEmpty()) continue;
					
					Date date = step.getUpdateTime();
					
					// It's possible to have more than one job per step, although we don't have any examples at the moment
					GalaxyJobState status = GalaxyJobState.UNKNOWN;
					String jobName = "";
					for(Job job : jobs) {
						// Concatenate the job names in case we have more than one. 
						jobName = jobName + job.getToolId() + " ";
						date = job.getCreated();
						status = getJobStatus(job.getState());
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
						result.setDate(date);
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
//						result.setOutputUrl(dataset.getFullDownloadUrl());
						
						// TODO add logic to populate tool version		
						
						result.setUpdateDate(new Date());
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
	 * This method is to refresh all galaxy jobs data in the dashbord table.  Only needed
	 * to prevent a cold start
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
					
					Date date = step.getUpdateTime();
					
					// It's possible to have more than one job per step, although we don't have any examples at the moment
					GalaxyJobState status = GalaxyJobState.UNKNOWN;
					String jobName = "";
					for(Job job : jobs) {
						// Concatenate the job names in case we have more than one. 
						jobName = jobName + job.getToolId() + " ";
						date = job.getCreated();
						status = getJobStatus(job.getState());
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
						result.setDate(date);
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
//						result.setOutputUrl(dataset.getFullDownloadUrl());
						
						// TODO add logic to populate tool version
						
						result.setUpdateDate(new Date());
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
	
}
