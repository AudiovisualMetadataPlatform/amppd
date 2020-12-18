package edu.indiana.dlib.amppd.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.WorkflowsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.GalaxyObject;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.ExistingHistory;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.InputSourceType;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.WorkflowInput;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.exception.GalaxyDataException;
import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.GalaxyDataService;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.service.WorkflowResultService;
import edu.indiana.dlib.amppd.web.WorkflowOutputResult;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of JobService.
 * @author yingfeng
 *
 */
@Service
@Slf4j
public class JobServiceImpl implements JobService {
	
	public static final String PRIMARYFILE_OUTPUT_HISTORY_NAME_PREFIX = "Output History for Primaryfile-";
	public static final String HMGM_TOOL_ID_PREFIX = "hmgm";
	public static final String HMGM_CONTEXT_PARAMETER_NAME = "context_json";
	public static final String FR_TOOL_ID_PREFIX = "dlib_face";
	public static final String FR_TRAIN_PARAMETER_NAME = "training_photos";
	
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
	
	@Autowired
	private AmpUserService ampUserService;
	
	@Autowired
    private MediaService mediaService;	
	
	@Autowired
    private WorkflowResultService workflowResultService;	
	
	@Getter
	private WorkflowsClient workflowsClient;
		
	@Getter
	private HistoriesClient historiesClient;
		
	/**
	 * Initialize the JobServiceImpl bean.
	 */
	@PostConstruct
	public void init() {
		workflowsClient = galaxyApiService.getGalaxyInstance().getWorkflowsClient();
		historiesClient = galaxyApiService.getGalaxyInstance().getHistoriesClient();
	}	
	
	/**
	 * Prepare the given primaryfile for AMP jobs, i.e. to run on a workflow in Galaxy: 
	 * if this is the first time it's ever run on any workflow, 
	 * - upload its media file to Galaxy using the symbolic link option and save the dataset ID into the primaryfile;
	 * - create a history for all workflow outputs associated with it and save the history ID into the primaryfile; 
	 * @param primaryfile the given primaryfile
	 * @return true if the primaryfile has been updated; false otherwise.
	 */
	protected boolean preparePrimaryfileForJobs(Primaryfile primaryfile) {
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
	    		throw new StorageException("Primaryfile " + primaryfile.getId() + " hasn't been uploaded to AMPPD file system");
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
				throw new GalaxyDataException("Cannot create Galaxy output history for primaryfile " + primaryfile.getId(), e);
			}		
		}			
		else {
			log.info("The Galaxy output history " + primaryfile.getHistoryId() + " for Primaryfile " + primaryfile.getId() + " already exists.");			
		}

		// if dataset or history IDs have been changed in primaryfile, persist it in DB 
		if (save) {
			primaryfileRepository.save(primaryfile);
			return true;
		}
		return false;
	}
	
	/**
	 * Build the workflow inputs to feed the given dataset and history along with the given user-defined parameters into the given Galaxy workflow.
	 * Note that the passed-in parameters here are user defined, and this method does not do any HMGM specific handling to the parameters.  
	 * @param workflowId ID of the given workflow
	 * @param datasetId ID of the given dataset
	 * @param historyId ID of the given history
	 * @param parameters step parameters for running the workflow
	 * @return the built WorkflowInputs instance
	 */
	protected WorkflowInputs buildWorkflowInputs(WorkflowDetails workflowDetails, String datasetId, String historyId, Map<String, Map<String, String>> parameters) {
		WorkflowInputs winputs = new WorkflowInputs();
		winputs.setDestination(new ExistingHistory(historyId));
		winputs.setImportInputsToHistory(false);
		winputs.setWorkflowId(workflowDetails.getId());
		
		String inputId;
		try {
			// each input in the workflow corresponds to an input step with a unique ID, the inputs of workflow detail is a map of {stepId: {label: value}}
			Set<String> inputIds = workflowDetails.getInputs().keySet();
			if (inputIds.size() != 1) {
				throw new GalaxyWorkflowException("Workflow " + workflowDetails.getId() + " has " + inputIds.size() + " inputs, while it should have exactly one input.");
			}
			
			// forAmppd, we can assume all workflows only take one primaryfile as input
			inputId = (String)inputIds.toArray()[0];
		}
		catch (Exception e) {
			throw new GalaxyWorkflowException("Exception when retrieving details for workflow " + workflowDetails.getId());
		}
		
		WorkflowInput winput = new WorkflowInput(datasetId, InputSourceType.LDDA);
		winputs.setInput(inputId, winput);		
		
		parameters.forEach((stepId, stepParams) -> {
			stepParams.forEach((paramName, paramValue) -> {
				winputs.setStepParameter(stepId, paramName, paramValue);
			});
		});
		
		log.info("Successfully built job inputs, workflowId: " + workflowDetails.getId() + ", datasetId: " + datasetId + " parameters: " + parameters);
		return winputs;
	}
	
	/**
	 * Populate parameters that need special handling for certain MGMs such as HMGM (Human MGM) and FR (Face Recognition):
	 * If the given workflow contains steps using HMGMs, generate context information needed by HMGM tasks and populate those 
	 * as json string into the context parameter of each HMGM step in the workflow;
	 * If the given workflow contains steps using FRs, translate the training_photos parameter from CollectionSupplement name 
	 * defined by user in the workflow, into the corresponding absolute pathname of the CollectionSupplement's media.
	 * Note that the passed-in parameters here are part of the workflow inputs already populated with user-defined values;
	 * while the context and absolute path parameters are system generated and shall be transparent to users.
	 * @param workflowDetails the given workflow
	 * @param primaryfile the given primaryfile
	 * @param parameters the parameters for the workflow
	 * @return a list of IDs of the steps for which parameters are added/changed
	 */
	protected List<String> populateMgmParameters(WorkflowDetails workflowDetails, Primaryfile primaryfile, Map<Object, Map<String, Object>> parameters) {
		// we store context in StringBuffer instead of String because foreach doesn't allow updating local variable defined outside its scope
		StringBuffer context = new StringBuffer(); 
		
		// IDs of the steps for which parameters are added/changed
		List<String> stepsChanged = new ArrayList<String>();		
		
		workflowDetails.getSteps().forEach((stepId, stepDef) -> {
			if (StringUtils.startsWith(stepDef.getToolId(), HMGM_TOOL_ID_PREFIX)) {
				// since all HMGMs in the workflow share the same context, we only need to compute it once when first needed, then reuse it
				if (context.length() == 0) {
					context.append(getHmgmContext(workflowDetails, primaryfile));
					log.info("Generated HMGM context for primaryfile + " + primaryfile.getId() + " and workflow " + workflowDetails.getId() + " is: " + context.toString());
				}
				
				// the context parameter shouldn't have been populated; if for some reason it is, it will be overwritten here anyways
				Map<String, Object> stepParams = parameters.get(stepId);
				if (stepParams == null) {
					stepParams = new HashMap<String, Object>();
					parameters.put(stepId, stepParams);
				}
				
				stepParams.put(HMGM_CONTEXT_PARAMETER_NAME, context.toString());
				stepsChanged.add(stepId);
				log.info("Added HMGM context for primaryfile: " + primaryfile.getId() + ", workflow: " + workflowDetails.getId() + ", step: " + stepId);
			}
			else if (StringUtils.startsWith(stepDef.getToolId(), FR_TOOL_ID_PREFIX)) {
				String msg =  ", for MGM " + stepDef.getToolId() + ", in step " + stepId + ", of workflow " + workflowDetails.getId() + ", with primaryfile " + primaryfile.getId();				

				// the training_photos parameter should have been populated with the supplement name associated with the primaryfile's collection,
				// either in the passed-in dynamic parameters, i.e. parameters provided upon workflow submission,
				// or in the static parameters, i.e. parameters defined in the steps of a workflow, which could be overwritten by the former.
				Map<String, Object> stepParams = parameters.get(stepId);				
				if (stepParams == null) {
					stepParams = new HashMap<String, Object>();
					parameters.put(stepId, stepParams);
				}
				
				// first look for the parameter in the passed in dynamic parameters
				String name = (String)stepParams.get(FR_TRAIN_PARAMETER_NAME);				
				// if not found, look in the static parameters in workflow detail
				if (StringUtils.isEmpty(name)) {
					name = (String)stepDef.getToolInputs().get(FR_TRAIN_PARAMETER_NAME);
				}				
				// if still not found, throw error
				if (StringUtils.isEmpty(name)) {
					throw new GalaxyWorkflowException("No training photos supplement name is defined in the workflow parameters" + msg);
				}
				
				// now the parameter is found, get the collection supplement's absolute pathname, given its name and the associated primaryfile
				String pathname = mediaService.getSupplementPathname(primaryfile, name, SupplementType.COLLECTION);
				if (StringUtils.isEmpty(pathname)) {
					throw new GalaxyWorkflowException("Could not find the exact training photos collection supplement with the name defined: " + name + msg);
				}
				
				// now the supplement pathname is found, update the training_photos parameter
				stepParams.put(FR_TRAIN_PARAMETER_NAME, pathname);
				stepsChanged.add(stepId);
				log.info("Translated parameter " + FR_TRAIN_PARAMETER_NAME + " from supplement name " + name + " to filepath " + pathname + msg);				
			}
		});

		log.info("Successfully updated parameters for " + stepsChanged.size() + " steps in workflow " + workflowDetails.getId() + " running on primaryfile " + primaryfile.getId());
		return stepsChanged;	
	}
	
	/**
	 *@see edu.indiana.dlib.amppd.service.JobService.getHmgmContext(WorkflowDetails, Primaryfile)
	 */
	@Override
	public String getHmgmContext(WorkflowDetails workflowDetails, Primaryfile primaryfile) {
		// we need to sanitize all the names before putting them into the context map, 
		// as quotes in a name could interfere when context is passed as a paramter on command line   
		// furthermore, we better do this before serialize the context into JSON string,
		// as ObjectMapper might add escape char for double quotes
		
		Map<String, String> context = new HashMap<String, String>();
		context.put("submittedBy", ampUserService.getCurrentUsername());
		context.put("unitId", primaryfile.getItem().getCollection().getUnit().getId().toString());		
		context.put("unitName", sanitizeText(primaryfile.getItem().getCollection().getUnit().getName()));
		context.put("collectionId", primaryfile.getItem().getCollection().getId().toString());		
		context.put("collectionName", sanitizeText(primaryfile.getItem().getCollection().getName()));
		context.put("taskManager", primaryfile.getItem().getCollection().getTaskManager());
		context.put("itemId", primaryfile.getItem().getId().toString());		
		context.put("itemName", sanitizeText(primaryfile.getItem().getName()));
		context.put("primaryfileId", primaryfile.getId().toString());
		context.put("primaryfileName", sanitizeText(primaryfile.getName()));
		context.put("primaryfileUrl", mediaService.getPrimaryfileMediaUrl(primaryfile));
		context.put("primaryfileMediaInfo", mediaService.getAssetMediaInfoPath(primaryfile));
		context.put("workflowId", workflowDetails.getId());		
		context.put("workflowName", sanitizeText(workflowDetails.getName()));	
		
		ObjectMapper objectMapper = new ObjectMapper();
		String contextJson = null;
		try {
			contextJson = objectMapper.writeValueAsString(context);
		}
		catch (Exception e) {			
            throw new RuntimeException("Error while converting context map to json string for primaryfile " + primaryfile.getId() + " and workflow " + workflowDetails.getId(), e);
        }
		
		return contextJson;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.sanitizeText(String)
	 */
	public String sanitizeText(String text) {
		char[] invalids = new char[] {'\'', '"'};
		
		// replace invalid chars with their hex code
		String str = text;
		for (char invalid : invalids) {
			str = StringUtils.replace(str, Character.toString(invalid), "%" + Integer.toHexString((int) invalid));
		}
		
		return str;
	}
	private WorkflowOutputResult createJobResult(Primaryfile primaryfile) {
		WorkflowOutputResult result = new WorkflowOutputResult();
		Item item = primaryfile.getItem();
		Collection c = item.getCollection();
	
		result.setCollectionLabel(c.getName());
		result.setPrimaryfileId(primaryfile.getId());
		result.setFileLabel(primaryfile.getName());
		result.setFileName(primaryfile.getOriginalFilename());
		result.setItemLabel(item.getName());
		
		return result;
	}
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJob(String,Long,Map<String, Map<String, String>>)
	 */	
	@Override
	public WorkflowOutputResult createJob(String workflowId, Long primaryfileId, Map<String, Map<String, String>> parameters) {		
		WorkflowOutputs woutputs = null;
		String msg = "Amppd job for: workflowId: " + workflowId + ", primaryfileId: " + primaryfileId;
		String msg_param = ", parameters (user defined): " + parameters;
		log.info("Creating " + msg + msg_param);
		
		// retrieve primaryfile via ID
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfileId + "> does not exist!"));
		
		// get the Galaxy history created and input dataset uploaded
		preparePrimaryfileForJobs(primaryfile);	
		
		// initialize job creation response
		WorkflowOutputResult result = createJobResult(primaryfile);

		// invoke the workflow 
    	try {
			WorkflowDetails workflowDetails = workflowsClient.showWorkflow(workflowId);
			if (workflowDetails == null) {
				throw new GalaxyWorkflowException("Can't find workflow with ID " + workflowId);
			}
			
    		WorkflowInputs winputs = buildWorkflowInputs(workflowDetails, primaryfile.getDatasetId(), primaryfile.getHistoryId(), parameters);
    		populateMgmParameters(workflowDetails, primaryfile, winputs.getParameters());
    		msg_param = ", parameters (system updated): " + winputs.getParameters();
    		woutputs = workflowsClient.runWorkflow(winputs);    		
    		
    		// add workflow results to the table for the newly created invocation
    		workflowResultService.addWorkflowResults(woutputs, workflowDetails, primaryfile);
    		
    		// set up result response
    		result.setResult(woutputs);
    		result.setSuccess(true);
    		log.info("Successfully created " + msg + msg_param);
        	log.info("Galaxy workflow outputs: " + woutputs);
    	}
    	catch (Exception e) {    	
    		log.error("Error creating " + msg + msg_param, e);	
    		result.setError(e.toString());
    		//throw new GalaxyWorkflowException("Error creating " + msg, e);
    	}
    	
    	return result;
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJobs(String, Long[], Map<String, Map<String, String>>)
	 */
	public List<WorkflowOutputResult> createJobs(String workflowId, Long[] primaryfileIds, Map<String, Map<String, String>> parameters) {
		List<WorkflowOutputResult> woutputs = new ArrayList<WorkflowOutputResult>();
		String msg = "a list of Amppd jobs for: workflowId: " + workflowId + ", primaryfileIds: " + primaryfileIds + ", parameters: " + parameters;
		log.info("Creating " + msg);		

		// remove redundant primaryfile IDs
		Set<Long> pidset = primaryfileIds == null ? new HashSet<Long>() : new HashSet<Long>(Arrays.asList(primaryfileIds));
		Long[] pids = pidset.toArray(primaryfileIds);		
		int nSuccess = 0;
		int nFailed = 0;

		for (Long primaryfileId : pids) {
			// skip null primaryfileId, which could result from redundant IDs passed from request parameter being changed to null
			if (primaryfileId == null) continue; 
			
			try {
				Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("primaryfile <" + primaryfileId + "> does not exist!"));    
				woutputs.add(createJob(workflowId, primaryfile.getId(), parameters));
				nSuccess++;
			}
			catch (Exception e) {
				// if error occurs with this primaryfile we still want to continue with other primaryfiles
				log.error("Error creating Amppd job for primaryfile " + primaryfileId, e);	
				nFailed++;
			}
		}  	

		log.info("Number of Amppd jobs successfully created for the primaryfiles: " + nSuccess);    	
		log.info("Number of Amppd jobs failed to be created: " + nFailed);    	
    	return woutputs;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJobBundle(String,Long,Map<String, Map<String, String>>)
	 */	
	@Override
	public List<WorkflowOutputResult> createJobBundle(String workflowId, Long bundleId, Map<String, Map<String, String>> parameters) {
		List<WorkflowOutputResult> woutputsMap = new ArrayList<WorkflowOutputResult>();
		String msg = "a bundle of Amppd jobs for: workflowId: " + workflowId + ", bundleId: " + bundleId + ", parameters: " + parameters;
		log.info("Creating " + msg);
		
		int nSuccess = 0;
		int nFailed = 0;
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("Bundle <" + bundleId + "> does not exist!"));        	
    	if (bundle.getPrimaryfiles() == null || bundle.getPrimaryfiles().isEmpty()) {
    		log.warn("Bundle <\" + bundleId + \"> does not contain any primaryfile.");
    	}
    	else { 
    		for (Primaryfile primaryfile : bundle.getPrimaryfiles() ) {
    			try {
    				woutputsMap.add(createJob(workflowId, primaryfile.getId(), parameters));
    				nSuccess++;
    			}
    			catch (Exception e) {
    				// if error occurs with this primaryfile we still want to continue with other primaryfiles
    				log.error("Error creating Amppd job for primaryfile " + primaryfile.getId(), e);		
    				nFailed++;
    			}
    		}
    	}	  	

		log.info("Number of Amppd jobs successfully created for the bundle: " + nSuccess);    	
		log.info("Number of Amppd jobs failed to be created: " + nFailed);    	
    	return woutputsMap;
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.listJobs(String,Long)
	 */	
	@Override	
	public List<Invocation> listJobs(String workflowId, Long primaryfileId) {
		List<Invocation> invocations =  new ArrayList<Invocation>();
		// retrieve primaryfile via ID
		Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfileId + "> does not exist!"));

		// return an empty list if no AMP job has been run on the workflow-primaryfile
		if (primaryfile.getHistoryId() == null) {
			log.warn("No AMP job has been run on workflow " + workflowId + " against primaryfile " + primaryfileId);
			return invocations;
		}

		try {
			invocations = workflowsClient.indexInvocations(workflowId, primaryfile.getHistoryId());
		} 
		catch(Exception e) {
			String msg = "Unable to index invocations for: workflowId: " + workflowId + ", priamryfileId: " + primaryfileId;
			log.error(msg);
			throw new GalaxyWorkflowException(msg, e);
		}
		
		log.info("Found " + invocations.size() + " invocations for: workflowId: " + workflowId + ", primaryfileId: " + primaryfileId);
		return invocations;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.showJobStepOutput(String, String, String, String)
	 */	
	@Override	
	public Dataset showJobStepOutput(String workflowId, String invocationId, String stepId, String datasetId) {
		Dataset dataset  = null;
		
		try {
			Invocation invocation = workflowsClient.showInvocation(workflowId, invocationId, false);
			dataset = historiesClient.showDataset(invocation.getHistoryId(), datasetId);
		}
		catch (Exception e) {
			String msg = "Could not find valid invocation for: workflowId: " + workflowId + ", invocationId: " + invocationId;
			log.error(msg);
			throw new GalaxyWorkflowException(msg, e);
		}

		log.debug("Found dataset for: workflowId: " + workflowId + ", invocationId: " + invocationId + ", datasetId: " + datasetId);
		return dataset;
	}
	
}
