package edu.indiana.dlib.amppd.service.impl;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.InputSourceType;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.WorkflowDestination;

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
	
	private GalaxyInstance galaxyInstance;
	
	@Getter
	private WorkflowsClient workflowsClient;
	
	@Getter
	private WorkflowDestination sharedHistory;
	
	/**
	 *  initialize Galaxy data library, which is shared by all AMPPD users.
	 */
	@PostConstruct
	public void init() {
		galaxyInstance = galaxyApiService.getGalaxyInstance();
		workflowsClient = galaxyInstance.getWorkflowsClient();
		sharedHistory = new WorkflowInputs.NewHistory(SHARED_HISTORY_NAME);
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.buildWorkflowInputs(String,String,Map<String, Map<String, String>>)
	 */	
	public WorkflowInputs buildWorkflowInputs(String workflowId, String datasetId, Map<String, Map<String, String>> parameters) {
		WorkflowInputs winputs = new WorkflowInputs();
		winputs.setDestination(sharedHistory);
		winputs.setImportInputsToHistory(false);
		winputs.setWorkflowId(workflowId);
		
		// assume we only take one primaryfile as input for any workflow
		String iname;
		try {
			WorkflowDetails wdetails = workflowsClient.showWorkflow(workflowId);
			if (wdetails == null) {
				throw new RuntimeException("Can't find workflow with ID " + workflowId);
			}
			Set<String> inames = wdetails.getInputs().keySet();
			if (inames.size() != 1) {
				throw new RuntimeException("Workflow " + workflowId + " doesn't have exactly one input.");
			}
			iname = (String)inames.toArray()[0];
		}
		catch (Exception e) {
			throw new RuntimeException("Exception when retrieving details for workflow " + workflowId);
		}
		WorkflowInputs.WorkflowInput winput = new WorkflowInputs.WorkflowInput(datasetId, InputSourceType.LDDA);
		winputs.setInput(iname, winput);		
		
		parameters.forEach((stepId, stepParams) -> {
			stepParams.forEach((paramName, paramValue) -> {
				winputs.setStepParameter(stepId, paramName, paramValue);
			});
		});
		
		log.info("Successfully built workflow inputs, workflow ID: " + workflowId + ", datasetId: " + datasetId);
		return winputs;
	}

}
