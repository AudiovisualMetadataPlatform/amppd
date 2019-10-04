package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.GalaxyObject;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.ExistingHistory;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.InputSourceType;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.WorkflowInput;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
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
	
	public static final String PRIMARYFILE_OUTPUT_HISTORY_NAME_PREFIX = "Output History for Primaryfile-";
	
	@Autowired
    private BundleRepository bundleRepository;

	@Autowired
    private PrimaryfileRepository primaryfileRepository;

	@Autowired
    private FileStorageService fileStorageService;	

	@Autowired
	private GalaxyApiService galaxyApiService;
	
	@Autowired
	private GalaxyDataService galaxyDataService;
	
	@Getter
	private WorkflowsClient workflowsClient;
		
	/**
	 * Initialize the JobServiceImpl bean.
	 */
	@PostConstruct
	public void init() {
		workflowsClient = galaxyApiService.getGalaxyInstance().getWorkflowsClient();
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.buildWorkflowInputs(String,String,String,Map<String, Map<String, String>>)
	 */	
	@Override
	public WorkflowInputs buildWorkflowInputs(String workflowId, String datasetId, String historyId, Map<String, Map<String, String>> parameters) {
		WorkflowInputs winputs = new WorkflowInputs();
//		winputs.setDestination(new ExistingHistory(galaxyDataService.getSharedHistory().getId()));
		winputs.setDestination(new ExistingHistory(historyId));
		winputs.setImportInputsToHistory(false);
		winputs.setWorkflowId(workflowId);
		
		String inputId;
		try {
			WorkflowDetails wdetails = workflowsClient.showWorkflow(workflowId);
			if (wdetails == null) {
				throw new GalaxyWorkflowException("Can't find workflow with ID " + workflowId);
			}
			
			// each input in the workflow corresponds to an input step with a unique ID, the inputs of workflow detail is a map of {stepId: {label: value}}
			Set<String> inputIds = wdetails.getInputs().keySet();
			if (inputIds.size() != 1) {
				throw new GalaxyWorkflowException("Workflow " + workflowId + " has " + inputIds.size() + " inputs, while it should have exactly one input.");
			}
			
			// forAmppd, we can assume all workflows only take one primaryfile as input
			inputId = (String)inputIds.toArray()[0];
		}
		catch (Exception e) {
			throw new GalaxyWorkflowException("Exception when retrieving details for workflow " + workflowId);
		}
		
		WorkflowInput winput = new WorkflowInput(datasetId, InputSourceType.LDDA);
		winputs.setInput(inputId, winput);		
		
		parameters.forEach((stepId, stepParams) -> {
			stepParams.forEach((paramName, paramValue) -> {
				winputs.setStepParameter(stepId, paramName, paramValue);
			});
		});
		
		log.info("Successfully built job inputs, workflow ID: " + workflowId + ", datasetId: " + datasetId + " parameters: " + parameters);
		return winputs;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJob(String,Long,Map<String, Map<String, String>>)
	 */	
	@Override
	public WorkflowOutputs createJob(String workflowId, Long primaryfileId, Map<String, Map<String, String>> parameters) {
		WorkflowOutputs woutputs = null;
		String msg = "Amppd job for: workflow ID: " + workflowId + ", primaryfileId: " + primaryfileId + " parameters: " + parameters;
		log.info("Creating " + msg);
		
		// retrieve primaryfile via ID
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfileId + "> does not exist!"));
		boolean save = false;

		/* Note: 
    	 * We do a lazy upload from Amppd to Galaxy, i.e. we only upload the primaryfile to Galaxy when a workflow is invoked in Galaxy against the primaryfile, 
    	 * rather than when the primaryfile is uploaded to Amppd.
    	 * The pros is that we won't upload to Galaxy unnecessarily if the primaryfile is never going to be processed through workflow;
    	 * the cons is that it might slow down workflow execution when running in batch.
		 * Furthermore, we only do this upload once, i.e. if the primaryfile has never been uploaded to Galaxy. 
		 * Later invocation of workflows on this primaryfile will just reuse the result from the first upload in Galaxy.
		 */
		if (primaryfile.getDatasetId() == null) {    	
	    	// at this point the primaryfile shall have been created and its media file uploaded into Amppd file system
	    	if (primaryfile.getPathname() == null || primaryfile.getPathname().isEmpty()) {
	    		throw new StorageException("Primaryfile " + primaryfileId + " hasn't been uploaded to AMPPD file system");
	    	}
	    	
	    	// upload the primaryfile into Galaxy data library, the returned result is a GalaxyObject containing the ID and URL of the dataset uploaded
	    	String pathname = fileStorageService.absolutePathName(primaryfile.getPathname());
	    	GalaxyObject go = galaxyDataService.uploadFileToGalaxy(pathname);	
	    	
	    	// set flag to save the dataset ID in primaryfile for future reuse
	    	primaryfile.setDatasetId(go.getId());
	    	save = true;
		}
		
		// if the output history hasn't been created for this primaryfile, i.e. it's the first time any workflow is run against it, create a new history for it
		if (primaryfile.getHistoryId() == null) {   
			// since we use primaryfile ID in the output history name, we can assume that the name is unique, 
			// thus, if the historyId is null, it means the output history for this primaryfile doesn't exist in Galaxy yet, and vice versa
			History history = new History(PRIMARYFILE_OUTPUT_HISTORY_NAME_PREFIX + primaryfile.getId());
			try {
				history = galaxyDataService.getHistoriesClient().create(history);
		    	primaryfile.setHistoryId(history.getId());		
		    	save = true;
				log.info("Initialized the Galaxy output history " + history.getId() + " for primaryfile " + primaryfile.getId());
			}
			catch (Exception e) {
				throw new RuntimeException("Cannot create Galaxy output history for primaryfile " + primaryfile.getId(), e);
			}		
		}			
		else {
			log.info("The Galaxy output history " + primaryfile.getHistoryId() + " for Primaryfile " + primaryfile.getId() + " already exists.");			
		}

		// if dataset or history IDs have been changed in primaryfile, persist it in DB 
		if (save) {
			primaryfileRepository.save(primaryfile);
		}

		// invoke the workflow 
    	try {
    		WorkflowInputs winputs = buildWorkflowInputs(workflowId, primaryfile.getDatasetId(), primaryfile.getHistoryId(), parameters);
    		woutputs = workflowsClient.runWorkflow(winputs);
    	}
    	catch (Exception e) {    	
    		log.severe("Error creating " + msg);
    		throw new GalaxyWorkflowException("Error creating " + msg, e);
    	}
    	
		log.info("Successfully created " + msg);
    	log.info("Galaxy workflow outputs: " + woutputs);
    	return woutputs;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJobBundle(String,Long,Map<String, Map<String, String>>)
	 */	
	@Override
	public List<WorkflowOutputs> createJobBundle(String workflowId, Long bundleId, Map<String, Map<String, String>> parameters) {
		List<WorkflowOutputs> woutputsList = new ArrayList<WorkflowOutputs>();
		String msg = "a bundle of Amppd jobs for: workflow ID: " + workflowId + ", bundleId: " + bundleId + ", parameters: " + parameters;
		log.info("Creating " + msg);
		
		int nSuccess = 0;
		int nFailed = 0;
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("Bundle <" + bundleId + "> does not exist!"));        	
    	if (bundle.getItems() == null || bundle.getItems().isEmpty()) {
    		log.warning("Bundle <\" + bundleId + \"> does not contain any item.");
    	}
    	else { 
	    	for (Item item : bundle.getItems()) {
	        	if (item.getPrimaryfiles() == null || item.getPrimaryfiles().isEmpty()) {
	        		log.warning("Item <\" + itemId + \"> does not contain any primaryfile.");
	        	}        	
	        	else {
		        	for (Primaryfile primaryfile : item.getPrimaryfiles() ) {
		        		try {
		        			woutputsList.add(createJob(workflowId, primaryfile.getId(), parameters));
		        			nSuccess++;
		        		}
		        		catch (Exception e) {
		        			// if error occurs with this primaryfile we still want to continue with other primaryfiles
		        			log.severe(e.getStackTrace().toString());	
		        			nFailed++;
		        		}
		        	}
	        	}
	    	}    		
    	}

		log.info("Number of Amppd jobs successfully created for the bundle: " + nSuccess);    	
		log.info("Number of Amppd jobs failed to be created: " + nFailed);    	
    	return woutputsList;
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.listJobs(String,Long)
	 */	
	@Override	
	public List<Invocation> listJobs(String workflowId, Long primaryfileId) {
		List<Invocation> invocations =  new ArrayList<Invocation>();
		// retrieve primaryfile via ID
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfileId + "> does not exist!"));

		// return an empty list if no AMP job as been run on the workflow-primaryfile
		if (primaryfile.getHistoryId() == null) {
			log.info("No AMP job has been run on workflow " + workflowId + " against primaryfile " + primaryfileId);
			return invocations;
		}

		invocations = workflowsClient.indexInvocations(workflowId, primaryfile.getHistoryId());
		log.info("Found " + invocations.size() + " invocations for: workflow ID: " + workflowId + ", primaryfile ID: " + primaryfileId);
		return invocations;
	}
	
}
