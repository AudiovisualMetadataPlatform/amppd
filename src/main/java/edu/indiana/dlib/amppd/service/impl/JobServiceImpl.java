package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
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
import org.springframework.web.multipart.MultipartFile;

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
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import edu.indiana.dlib.amppd.exception.GalaxyDataException;
import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.exception.ParserException;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.service.GalaxyDataService;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.service.WorkflowResultService;
import edu.indiana.dlib.amppd.web.CreateJobResponse;
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
    private WorkflowResultRepository workflowResultRepository;

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
	protected Boolean preparePrimaryfileForJobs(Primaryfile primaryfile) {	
		Boolean save = false;

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
	 * Check whether the given workflow results outputs are valid to be used as workflow inputs, i.e. they all exist
	 * and share the same primaryfileId and historyId, and share those with the provided primaryfile if not null; 
	 * if all results are valid, add their outputIds to the given list, and return the shared primaryfile; 
	 * otherwise throw exception.
	 * @param primaryfile ID the given primaryfile, could be null
	 * @param resultIds array of the IDs of the given workflow results, assumed not null but could be empty
	 * @param outputIds list of the outputIds of the given workflow results. assumed to be initialized to empty list
	 */
	protected Primaryfile retrieveSharedPrimaryfileValidateOutputs(Primaryfile primaryfile, Long[] resultIds, List<String> outputIds) {
		Long primaryfileId = primaryfile == null ? null : primaryfile.getId();
		String historyId = primaryfile == null ? null : primaryfile.getHistoryId();
 
		for (Long resultId : resultIds) {
			// retrieve WorkflowResult by ID and make sure the outputId is populated
			WorkflowResult result = workflowResultRepository.findById(resultId).orElseThrow(() -> new StorageException("WorkflowResult <" + resultId + "> does not exist!"));
			String outputId = result.getOutputId();
			if (StringUtils.isEmpty(outputId)) {
				throw new StorageException("WorkflowResult " + resultId + " has empty outputId!");
			}
			
			// all provided results must share the same non-empty primaryfileId
			if (result.getPrimaryfileId() == null) {
				throw new GalaxyWorkflowException("WorkflowResult " + resultId + " has empty primaryfileId!");
			}
			else if (primaryfileId == null) {
				// assign the first non-empty primaryfileId among the results as the shared one to compare against
				primaryfileId = result.getPrimaryfileId();
			}
			else if (!primaryfileId.equals(result.getPrimaryfileId())) {
				throw new GalaxyWorkflowException("WorkflowResult " + resultId + " has a different primaryfileId " + result.getPrimaryfileId() + " than the shared " + primaryfileId);
			}
			
			// all provided results must share the same non-null historyId 
			if (result.getHistoryId() == null) {
				throw new GalaxyWorkflowException("WorkflowResult " + resultId + " has empty historyId!");
			}
			else if (historyId == null) {
				// assign the first non-empty historyId among the results as the shared one to compare against
				historyId = result.getHistoryId();
			}
			else if (!historyId.equals(result.getHistoryId())) {
				throw new GalaxyWorkflowException("WorkflowResult " + resultId + " has a different historyId " + result.getPrimaryfileId() + " than the shared " + historyId);
			}
			
			outputIds.add(outputId);
		}
		
		// at least one of the input source, i.e. primaryfile or results, must be provided
		if (primaryfileId == null || historyId == null) {
			throw new GalaxyWorkflowException("No valid primaryfile or previous results are provided as the workflow inputs!");    			
		}
		
		// if passed in primaryfile is not null, use it; otherwise retrieve the shared primaryfile 
		// to make sure it actually exists and has the same historyId as shared by the results
		if (primaryfile == null) {
			Long id = primaryfileId; // Java compiler disallows using non-final local variable such as primaryfileId in below line
			primaryfile = primaryfileRepository.findById(id).orElseThrow(() -> new StorageException("Primaryfile <" + id + "> does not exist!"));
			if (!primaryfile.getHistoryId().equals(historyId)) {
				throw new GalaxyWorkflowException("Primaryfile " + id + " shared by the results has a different historyId " + primaryfile.getHistoryId() + " than the shared " + historyId);				
			}
		}

		log.info("Succesfully validated and retrieved " + outputIds.size() + " workflow result outputs for the shared primaryfile " + primaryfileId);
		return primaryfile;
	}
	
	/**
	 * Build the inputs for the given workflow in Galaxy, by feeding them with the given primaryfile's dataset,
	 * and the outputs of the given workflow results, with the given user-defined parameters, in the given history.
	 * Note that the passed-in parameters here are user defined, and this method does not do any HMGM specific handling to the parameters.  
	 * @param workflowDetails the given workflow details
	 * @param datasetId dataset ID of the primaryfile
	 * @param outputIds list of the output IDs of the given workflow results
	 * @param historyId ID of the given history
	 * @param parameters step parameters for running the workflow
	 * @return the built WorkflowInputs instance
	 */
	protected WorkflowInputs buildWorkflowInputs(WorkflowDetails workflowDetails, String datasetId, List<String> outputIds, String historyId, Map<String, Map<String, String>> parameters) {
		// count total number of provided inputs
		int count = datasetId == null ? outputIds.size() : outputIds.size() + 1;
		
		// each input in the workflow corresponds to an input step with a unique ID, the inputs of workflow detail is a map of {stepId: {label: value}}
		Set<String> inputIds = workflowDetails.getInputs().keySet();
		
		// the total number of provided inputs must equal the total number of expected inputs in the workflow
		if (inputIds.size() != count) {
			throw new GalaxyWorkflowException("Workflow " + workflowDetails.getId() + " expects " + inputIds.size() + " inputs, but a total of " + count + " inputs are provided.");
		}

		WorkflowInputs winputs = new WorkflowInputs();
		winputs.setDestination(new ExistingHistory(historyId));
		winputs.setImportInputsToHistory(false);
		winputs.setWorkflowId(workflowDetails.getId());

		// if primaryfile dataset is provided, it's assumed to be the first input in the workflow, which has key "0"
		Integer index = 0;
		if (datasetId != null) {
			String inputId = (index++).toString();
			WorkflowInput winput = new WorkflowInput(datasetId, InputSourceType.LDDA);
			winputs.setInput(inputId, winput);		
		}

		// if outputs are provided, it's assumed that they are in the order of the input keys, starting after primaryfile input if any
		for (String outputId : outputIds) {
			String inputId = (index++).toString();
			WorkflowInput winput = new WorkflowInput(outputId, InputSourceType.HDA);
			winputs.setInput(inputId, winput);		
		}

		parameters.forEach((stepId, stepParams) -> {
			stepParams.forEach((paramName, paramValue) -> {
				winputs.setStepParameter(stepId, paramName, paramValue);
			});
		});

		String datasetMsg = datasetId == null ? "" : ", datasetId: " + datasetId;
		String outputsMsg = outputIds.isEmpty() ? "" : ", outputIds: " + outputIds;
		log.info("Successfully built job inputs, workflowId: " + workflowDetails.getId() + datasetMsg + outputsMsg + ", parameters: " + parameters);
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
	
	/**
	 * Create an AMP job to invoke the given workflow in Galaxy on the given primaryfile and/or previous WorkflowResult outputs, 
	 * along with the given parameters, based on the given includePrimaryfile indicator:
	 * if resultIds is null, includePrimaryfile is ignored, only primaryfile will be used as the input;
	 * otherwise, if includePrimaryfile is true, include the primaryfile shared by the workflowResults as the first input, 
	 * with the outputs from the workflowResults as the following inputs in that order;
	 * otherwise (includePrimaryfile is false), exclude the primaryfile and use only the outputs as the workflow inputs.
	 * @param workflowDetails details of the given workflow, assumed not null
	 * @param primaryfileId ID of the given primaryfile, could be null
	 * @param resultIds array of IDs of the given WorkflowResults, could be null or empty
	 * @param parameters the dynamic parameters to use for the steps in the workflow as a map {stepId: {paramName; paramValue}}
	 * @param includePrimaryfile the given indicator on whether or not to include primaryfile as workflow input
	 * @return CreateJobResponse containing detailed information for the job submitted
	 */
	protected CreateJobResponse createJob(WorkflowDetails workflowDetails, Long primaryfileId, Long[] resultIds, Map<String, Map<String, String>> parameters, Boolean includePrimaryfile) {
		String primaryfileMsg = primaryfileId == null ? "" : ", primaryfileId: " + primaryfileId;
		String resultsMsg = "";
		if (resultIds != null) {
			resultsMsg = ", resultIds: [";
			for (Long resultId : resultIds) {
				resultsMsg += resultId + ",";
			}
			resultsMsg += "]";
		}
		String workflowId = workflowDetails.getId();
		String msg = "AMP job for: workflowId: " + workflowId + primaryfileMsg + resultsMsg;
		String msg_param = ", parameters (user defined): " + parameters;
		
		log.info("Creating " + msg + msg_param);		
		CreateJobResponse response = null;

		try {			
			// handle primaryfile as input if provided
			Primaryfile primaryfile = null;
    		if (primaryfileId != null) {
    			// initialize job creation response
    			response = new CreateJobResponse(primaryfileId);
    			
    			// retrieve primaryfile via ID
    			primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfileId + "> does not exist!"));
        		
    			// update name fields with primaryfile info in job creation response
        		response.setNames(primaryfile);
        		
        		// get the Galaxy history created and input dataset uploaded
        		preparePrimaryfileForJobs(primaryfile);	
    		}
    		
    		// handle previous results as input if provided
    		List<String> outputIds = new ArrayList<String>();
    		if (resultIds != null) {
    			// make sure that all results have valid outputs and share the same primaryfile and history
    			primaryfile = retrieveSharedPrimaryfileValidateOutputs(primaryfile, resultIds, outputIds);
        		
    			// if not done yet, initialize job creation response with primaryfile info
        		if (response == null) {
        			response = new CreateJobResponse(primaryfile.getId());
        			response.setNames(primaryfile);
        		}
    		}
    		
    		/* TODO 
    		 * We could do more intensive validation on primaryfile and results, i.e.
    		 * check if their data types fit the data types of the workflow inputs.
    		 * To do that, we need to scan each workflow step, get the input_step of each step,
    		 * retrieve the data type of each input using the tool ID and the input_name, and
    		 * use the source_step value to match the input key, which should correspond to the index of the resultIds;
    		 * the data type of each result is available in the corresponding workflow result.
    		 * This process would take multiple Galaxy calls (one for each tool/input), so it could be expensive.
    		 * If the results come from UI, we can include the validation in the front end implicitly by restricting
    		 * the results user can choose for each workflow input. This saves redundant validation on the backend.
    		 * It's also possible to skip Galaxy tool/input lookup, but match output-input by output name and input_name directly.
    		 * This might need some hard-coded mapping on AMP side, as the input/output name is not necessarily 1:1 mapping. 
    		 */

    		// build inputs and invoke the workflow 
    		// when resultIds is not provided, includePrimaryfile will be ignored, and primaryfile will be the only input 
			String datasetId = resultIds == null || includePrimaryfile ? primaryfile.getDatasetId() : null; 
			WorkflowInputs winputs = buildWorkflowInputs(workflowDetails, datasetId, outputIds, primaryfile.getHistoryId(), parameters);
    		populateMgmParameters(workflowDetails, primaryfile, winputs.getParameters());
    		msg_param = ", parameters (system updated): " + winputs.getParameters();
    		WorkflowOutputs woutputs = workflowsClient.runWorkflow(winputs);    		
    		
    		// add workflow results to the table for the newly created invocation
    		workflowResultService.addWorkflowResults(woutputs, workflowDetails, primaryfile);
    		
    		// update response with success job creation status
    		response.setStatus(true, "", woutputs);
    		log.info("Successfully created " + msg + msg_param);
        	log.info("Galaxy workflow outputs: " + woutputs);
    	}
    	catch (Exception e) {  
    		String error = "";
			// if not done yet, initialize job creation response with primaryfile info
    		if (response == null) {
    			response = new CreateJobResponse(0l); // use an invalid primaryfileId since no primaryfile was retrieved
    			error = "Failed to retrieve/validate primaryfile/results!\n";
    		}
    		// update response with failure job creation status
    		response.setStatus(false, error + e.toString(), null);
    		log.error("Failed to create " + msg + msg_param, e);	
    	}
    	
    	return response;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJob(WorkflowDetails, Long, Map<String, Map<String, String>>)
	 */	
	@Override
	public CreateJobResponse createJob(WorkflowDetails workflowDetails, Long primaryfileId, Map<String, Map<String, String>> parameters) {
		return createJob(workflowDetails, primaryfileId, null, parameters, true);
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJob(WorkflowDetails, Long[], Map<String, Map<String, String>>, Boolean)
	 */
	public CreateJobResponse createJob(WorkflowDetails workflowDetails, Long[] resultIds, Map<String, Map<String, String>> parameters, Boolean includePrimaryfile) {
		return createJob(workflowDetails, null, resultIds, parameters, includePrimaryfile);
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJobs(String, Long[], Map<String, Map<String, String>>)
	 */
	public List<CreateJobResponse> createJobs(String workflowId, Long[] primaryfileIds, Map<String, Map<String, String>> parameters) {
		log.info("Creating a list of AMP jobs for: workflowId: " + workflowId + ", primaryfileIds: " + primaryfileIds + ", parameters: " + parameters);		

		// initialize responses
		List<CreateJobResponse> responses = new ArrayList<CreateJobResponse>();
		int nSuccess = 0;

		// retrieve the workflow 
		WorkflowDetails workflowDetails = workflowsClient.showWorkflow(workflowId);
		if (workflowDetails == null) {
			throw new GalaxyWorkflowException("Can't find workflow with ID " + workflowId);
			// TODO find a good way to return error instead of exception
		}
		
		// remove redundant primaryfile IDs
		Set<Long> pidset = primaryfileIds == null ? new HashSet<Long>() : new HashSet<Long>(Arrays.asList(primaryfileIds));
		Long[] pids = pidset.toArray(primaryfileIds);		

		// create AMP job for each primaryfile in the array
		for (Long primaryfileId : pids) {
			// skip null primaryfileId, which could result from redundant IDs passed from request parameter being changed to null
			if (primaryfileId == null) continue; 

			// no need to catch exception as createJob catches all and always returns a response 
			CreateJobResponse response = createJob(workflowDetails, primaryfileId, parameters);
			responses.add(response);
			if (response.getSuccess()) {
				nSuccess++;
			}
		}  	

		log.info("Number of AMP jobs successfully created for the primaryfiles: " + nSuccess);    	
		log.info("Number of AMP jobs failed to be created for the primaryfiles: " + (pids.length - nSuccess));    	
    	return responses;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJobBundle(String, Long, Map<String, Map<String, String>>)
	 */	
	@Override
	public List<CreateJobResponse> createJobBundle(String workflowId, Long bundleId, Map<String, Map<String, String>> parameters) {
		log.info("Creating a bundle of AMP jobs for: workflowId: " + workflowId + ", bundleId: " + bundleId + ", parameters: " + parameters);
		
		// initialize responses
		List<CreateJobResponse> responses = new ArrayList<CreateJobResponse>();
		int nSuccess = 0;

		// retrieve the workflow 
		WorkflowDetails workflowDetails = workflowsClient.showWorkflow(workflowId);
		if (workflowDetails == null) {
			throw new GalaxyWorkflowException("Can't find workflow with ID " + workflowId);
			// TODO find a good way to return error instead of exception
		}
		
		// retrieve bundle
		Bundle bundle = bundleRepository.findById(bundleId).orElseThrow(() -> new StorageException("Bundle <" + bundleId + "> does not exist!"));        	
		if (bundle.getPrimaryfiles() == null || bundle.getPrimaryfiles().isEmpty()) {
			log.warn("Bundle <\" + bundleId + \"> does not contain any primaryfile, so no jobs will be created.");
			return responses;
		}

		// create AMP job for each primaryfile in the bundle
		Set<Primaryfile> primaryfiles = bundle.getPrimaryfiles();
		for (Primaryfile primaryfile : primaryfiles) {
			// no need to catch exception as createJob catches all and always returns a response 
			CreateJobResponse response = createJob(workflowDetails, primaryfile.getId(), parameters);
			responses.add(response);
			if (response.getSuccess()) {
				nSuccess++;
			}
		}

		log.info("Number of AMP jobs successfully created for the bundle: " + nSuccess);    	
		log.info("Number of AMP jobs failed to be created for the bundle: " + (primaryfiles.size() - nSuccess));    		  	
		return responses;
	}	
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJobs(String, Long[][], Map<String, Map<String, String>>, Boolean)
	 */
	public List<CreateJobResponse> createJobs(String workflowId, Long[][] resultIdss, Map<String, Map<String, String>> parameters, Boolean includePrimaryfile) {
		log.info("Creating AMP jobs for: workflowId: " + workflowId + ", resultIdss: " + resultIdss + ", parameters: " + parameters + ", includePrimaryfile: " + includePrimaryfile);

		// initialize responses
		List<CreateJobResponse> responses = new ArrayList<CreateJobResponse>();
		int nSuccess = 0;

		// retrieve the workflow 
		WorkflowDetails workflowDetails = workflowsClient.showWorkflow(workflowId);
		if (workflowDetails == null) {
			throw new GalaxyWorkflowException("Can't find workflow with ID " + workflowId);
			// TODO find a good way to return error instead of exception
		}
				
		// create job for each row in the csv
		for (int i=0; i < resultIdss.length; i++) {
			// no need to catch exception as createJob catches all and always returns a response 
			CreateJobResponse response = createJob(workflowDetails, null, resultIdss[i], parameters, includePrimaryfile);			
			responses.add(response);						
			if (response.getSuccess()) {
				nSuccess++;
			}			
		}
		
		log.info("Number of AMP jobs successfully created for the inputCsv: " + nSuccess);    	
		log.info("Number of AMP jobs failed to be created for the inputCsv: " + (resultIdss.length - nSuccess));    		  	
		return responses;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.createJobs(String, MultipartFile, Map<String, Map<String, String>>, Boolean)
	 */
	public List<CreateJobResponse> createJobs(String workflowId, MultipartFile inputCsv, Map<String, Map<String, String>> parameters, Boolean includePrimaryfile) {
		log.info("Creating AMP jobs for: workflowId: " + workflowId + ", inputCsv: " + inputCsv.getOriginalFilename() + ", parameters: " + parameters + ", includePrimaryfile: " + includePrimaryfile);

		// parse the input CSV into 2D array of WorkflowResults
		Long[][] resultIdss = parseInputCsv(inputCsv);
		// TODO find a good way to return error instead of exception from parseInputCsv

		// create jobs for WorkflowResults 2D array
		return createJobs(workflowId, resultIdss, parameters, includePrimaryfile); 
	}

	/**
	 * @see edu.indiana.dlib.amppd.service.JobService.parseInputCsv(MultipartFile)
	 */
	public Long[][] parseInputCsv(MultipartFile inputCsv) {		
		log.info("Parsing input CSV file " + inputCsv.getOriginalFilename() + " for workflow submission ...");
		
		List<String[]> rows = null;
		int nrow = 0;
		int ncol = 0;
		String errmsg = "Failed to parse the input CSV file " + inputCsv.getOriginalFilename() + " for workflow submission!";

		// overall process
		try {
			// parse the CSV File into a list of string arrays
			CSVReader reader = new CSVReaderBuilder(new BufferedReader(new InputStreamReader(inputCsv.getInputStream()))).build();
			rows = reader.readAll();
			
			// there should be at least the header row
			if (rows.isEmpty()) {
				throw new ParserException("The input CSV file has no row, at least the header row must be present!");				
			}	

			// record the number of columns in the CSV header
			ncol = rows.get(0).length;
			nrow = rows.size();
			
			// there should be the primaryfileId plus at least one workflowResultId column
			if (ncol < 2) {
				throw new ParserException("The input CSV file has no column for WorkflowResult IDs, besides the primaryfileId column, at least one WorkflowResultId column must be present!");				
			}				
		}
		catch(Exception e) {
			log.error(errmsg, e);
			throw new ParserException(errmsg + e.getMessage(), e);
		}	

		Long[][] resultIdss = new Long[nrow-1][ncol-1];	
		StringBuilder errors = new StringBuilder();
		
		// process each row after the header, append error message if any
		for (int i=1; i < rows.size(); i++) {
			String[] columns = rows.get(i);
			Long primaryfileId = null;
			
			try {
				// each row should have the same number of columns as the header
				if (columns.length != ncol) {
					throw new ParserException("There are " + columns.length + " instead of the expected " + ncol + " columns!");
				}

				// first column should be primaryfileId, we don't rely on its value but better validate its format
				primaryfileId = Long.parseLong(columns[0].trim());  

				// the rest of the columns should be resultIds in the order of workflow inputs
				for (int j=1; j < columns.length; j++) {
					resultIdss[i-1][j-1] = Long.parseLong(columns[j].trim());  
				}
			}
			catch(Exception e) {
				String err = "Error on row " + i + " for primaryfile " + primaryfileId + ": ";
				errors.append( "\n" + err + e.toString());
				log.error(err, e);
			}				
		}

		// throw parser exception if any error on the rows 
		if (errors.length() > 0) {
			String errmsgs = errmsg + errors.toString();
			log.error(errmsgs);
			throw new ParserException(errmsgs);
		}					

		log.info("Successfully parsed the input CSV file into " + (nrow-1) + " rows of " + (ncol-1) + " workflowResultIds");
		return resultIdss;
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

		log.trace("Found dataset for: workflowId: " + workflowId + ", invocationId: " + invocationId + ", datasetId: " + datasetId);
		return dataset;
	}
	
	
}
