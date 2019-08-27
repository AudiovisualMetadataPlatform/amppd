package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.List;
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
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowStepDefinition.WorkflowStepOutput;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowStepDefinition;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.GalaxyDataService;
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
	
//	public static final String GALAXY_WORKFLOW_INPUT_STEP_TYPE = "data_input";
	
	@Autowired
	private GalaxyApiService galaxyApiService;
	
	@Autowired
	private GalaxyDataService galaxyDataService;
	
	private GalaxyInstance galaxyInstance;
	
	@Getter
	private WorkflowsClient workflowsClient;
		
	/**
	 *  initialize Galaxy data library, which is shared by all AMPPD users.
	 */
	@PostConstruct
	public void init() {
		galaxyInstance = galaxyApiService.getGalaxyInstance();
		workflowsClient = galaxyInstance.getWorkflowsClient();
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.buildWorkflowInputs(String,String,Map<String, Map<String, String>>)
	 */
	public List<String> getWorkflowInputs(String workflowId) {
		 /* Note: 
		  * This method is an bugfix/extension to the blend4j WorkflowsClientImpl.showWorkflow, which unfortunately always returns empty list for the WorkflowDetails.inputs.
		  * For Amppd to submit a workflow, a map of input ID and the input dataset need to be passed to Galaxy. Due to above bug, we can't rely on the returned
		  * WorkflowDetails.inputs for this purpose; instead we have to acquire the list of inputs from the WorkflowDetails.steps, which as shown by our tests,
		  * does contain expected contents. Galaxy workflow list data input as one step in the workflow, with type 'data_input'. That's how we can decide if the step
		  * is an input, and collect those as the list of inputs for the workflow.
		  */
		
		ArrayList<String> inputs = new ArrayList<String>();		
		WorkflowDetails wdetails = workflowsClient.showWorkflow(workflowId);
		if (wdetails == null) {
			return null;
		}
		
		// the following code doesn't work either, because the workflow can contain only 'tool' type steps, in which case no step is of type 'data-input'
//		// search for the steps with type 'data_input'
//		int size = wdetails.getSteps().size();
//		for (Map.Entry<String, WorkflowStepDefinition> step : wdetails.getSteps().entrySet()) {
//			String type = step.getValue().getType();
//			Map<String, WorkflowStepOutput> sin = step.getValue().getInputSteps();
//			for (Map.Entry<String, WorkflowStepOutput> sentry : sin.entrySet()) {
//				String id = sentry.getKey();
//				WorkflowStepOutput wso =  sentry.getValue();
//			}
//			if (step.getValue().getType().equals(GALAXY_WORKFLOW_INPUT_STEP_TYPE)) {
//				inputs.add(step.getKey());
//			}
//		}
		
		// search for steps with empty input steps, which shall be identified as those need original inputs when workflow is invoked
		for (Map.Entry<String, WorkflowStepDefinition> step : wdetails.getSteps().entrySet()) {
			if (step.getValue().getInputSteps().isEmpty()) {
				inputs.add(step.getKey());
			}		
		}
		
		return inputs;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.WorkflowService.buildWorkflowInputs(String,String,Map<String, Map<String, String>>)
	 */	
	public WorkflowInputs buildWorkflowInputs(String workflowId, String datasetId, Map<String, Map<String, String>> parameters) {
		WorkflowInputs winputs = new WorkflowInputs();
		winputs.setDestination(new ExistingHistory(galaxyDataService.getSharedHistory().getId()));
		winputs.setImportInputsToHistory(false);
		winputs.setWorkflowId(workflowId);
		
		// assume all workflows only take one primaryfile as input
		String inputStepId;
		try {
			// the following code doesn't work because the inputs returned in workflow details is always empty
//			WorkflowDetails wdetails = workflowsClient.showWorkflow(workflowId);
//			if (wdetails == null) {
//				throw new GalaxyWorkflowException("Can't find workflow with ID " + workflowId);
//			}
//			Set<String> inames = wdetails.getInputs().keySet();
//			if (inames.size() != 1) {
//				throw new GalaxyWorkflowException("Workflow " + workflowId + " doesn't have exactly one input.");
//			}
//			iname = (String)inames.toArray()[0];
			
			List<String> inputs = getWorkflowInputs(workflowId);
			if (inputs == null) {
				throw new GalaxyWorkflowException("Workflow " + workflowId + " doesn't exist in Galaxy.");
			}
			if (inputs.size() != 1) {
				throw new GalaxyWorkflowException("Workflow " + workflowId + " doesn't have exactly one input.");
			}
			inputStepId = (String)inputs.get(0);
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
		winputs.setInput(inputStepId, winput);		
		
		parameters.forEach((stepId, stepParams) -> {
			stepParams.forEach((paramName, paramValue) -> {
				winputs.setStepParameter(stepId, paramName, paramValue);
			});
		});
		
		log.info("Successfully built workflow inputs, workflow ID: " + workflowId + ", datasetId: " + datasetId);
		return winputs;
	}

}
