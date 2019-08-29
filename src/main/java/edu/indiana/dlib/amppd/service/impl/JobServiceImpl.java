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
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.ExistingHistory;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.InputSourceType;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.WorkflowInput;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.GalaxyDataService;
import edu.indiana.dlib.amppd.service.JobService;
import lombok.Getter;
import lombok.extern.java.Log;

/**
 * Implementation of JobService.
 * @author yingfeng
 *
 */
@Service
@Log
public class JobServiceImpl implements JobService {
	
	@Autowired
	private GalaxyApiService galaxyApiService;
	
	@Autowired
	private GalaxyDataService galaxyDataService;
	
	@Getter
	private WorkflowsClient workflowsClient;
		
	/**
	 * Initialize Galaxy data library, which is shared by all AMPPD users.
	 */
	@PostConstruct
	public void init() {
		workflowsClient = galaxyApiService.getGalaxyInstance().getWorkflowsClient();
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.buildWorkflowInputs(String,String,Map<String, Map<String, String>>)
	 */	
	public WorkflowInputs buildWorkflowInputs(String workflowId, String datasetId, Map<String, Map<String, String>> parameters) {
		WorkflowInputs winputs = new WorkflowInputs();
		winputs.setDestination(new ExistingHistory(galaxyDataService.getSharedHistory().getId()));
		winputs.setImportInputsToHistory(false);
		winputs.setWorkflowId(workflowId);
		
		// assume all workflows only take one primaryfile as input
		String inputId;
		try {
			WorkflowDetails wdetails = workflowsClient.showWorkflow(workflowId);
			if (wdetails == null) {
				throw new GalaxyWorkflowException("Can't find workflow with ID " + workflowId);
			}
			
			Set<String> inputIds = wdetails.getInputs().keySet();
			if (inputIds.size() != 1) {
				throw new GalaxyWorkflowException("Workflow " + workflowId + " has " + inputIds.size() + " inputs, while it should have exactly one input.");
			}
			inputId = (String)inputIds.toArray()[0];
		}
		catch (Exception e) {
			throw new GalaxyWorkflowException("Exception when retrieving details for workflow " + workflowId);
		}
		
		/* TODO
		 * There is one remaining question about setting workflow inputs: all the APIs we know so far (blend4, bioblend, Galaxy REST API) indicates that 
		 * the inputs is a dictionary mapping each input step id to a dictionary with 2 keys: ‘src’ (which can be ‘ldda’, ‘ld’ or ‘hda’) and ‘id’ (the dataset ID).
		 * However, what if a step has multiple inputs? How to indicate which input of the step is referred to? There seems to be one key missing: the input label,
		 * which is seen on Galaxy API when querying a particular workflow: each input has a stepId, a label, and a value. 
		 * This might be an issue and shall be tested out if we have an MGM that accepts multiple inputs and it's an initial node in the workflow. 
		 */
		WorkflowInput winput = new WorkflowInput(datasetId, InputSourceType.LDDA);
		winputs.setInput(inputId, winput);		
		
		parameters.forEach((stepId, stepParams) -> {
			stepParams.forEach((paramName, paramValue) -> {
				winputs.setStepParameter(stepId, paramName, paramValue);
			});
		});
		
		log.info("Successfully built workflow inputs, workflow ID: " + workflowId + ", datasetId: " + datasetId);
		return winputs;
	}

}
