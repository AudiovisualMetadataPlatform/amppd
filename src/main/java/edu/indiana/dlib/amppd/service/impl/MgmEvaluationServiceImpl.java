package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest.TestStatus;
import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.repository.MgmEvaluationTestRepository;
import edu.indiana.dlib.amppd.repository.MgmScoringParameterRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.web.GalaxyJobState;
import edu.indiana.dlib.amppd.web.MgmEvaluationFilesObj;
import edu.indiana.dlib.amppd.web.MgmEvaluationParameterObj;
import edu.indiana.dlib.amppd.web.MgmEvaluationRequest;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class MgmEvaluationServiceImpl implements MgmEvaluationService {
    @Autowired
    private MgmEvaluationTestRepository mgmEvalRepo;
    @Autowired
    private PrimaryfileSupplementRepository supplementRepository;

    @Autowired
    private WorkflowResultRepository wfRepo;

    @Autowired
    private PrimaryfileRepository pfRepository;

    @Autowired
    private MgmScoringParameterRepository paramsRepo;

    @Autowired
    private MediaService mediaService;

    @Autowired
    AmppdPropertyConfig config;

    @Override
    public MgmEvaluationTestResponse getMgmEvaluationTests(MgmEvaluationSearchQuery query) {
        MgmEvaluationTestResponse response = mgmEvalRepo.findByQuery(query);
        log.info("Successfully retrieved " + response.getTotalResults() + " WorkflowResults for search query " + query);
        return response;
    }

    private List<String> validate(ArrayList<MgmEvaluationFilesObj> files, ArrayList<MgmEvaluationParameterObj> inputParams, MgmScoringTool mst){        
        ArrayList<String> errors = new ArrayList<>();
        if (files.size() <= 0) {
            errors.add("No Primary and Groundtruth files selected.");     
        }
        errors.addAll(validateFiles(files, mst));
        errors.addAll(validateRequiredParameters(inputParams, mst));
        
        for (String error : errors) {
            log.error(error);         	
        }
        
        return errors;
    }


    private List<String> validateFiles(ArrayList<MgmEvaluationFilesObj> files, MgmScoringTool mst) {
    	ArrayList<String> errors = new ArrayList<>();

    	for (MgmEvaluationFilesObj file : files) {
    		Long gtId = file.getGroundtruthFileId();
    		Long wrId = file.getWorkflowResultId();
    		boolean skip = false;

    		if (gtId == null) {
    			errors.add("Groundtruth file ID is missing.");
    			skip = true;
    		}
    		if (wrId == null) {
    			errors.add("Workflow result ID is missing.");
    			skip = true;
    		} 

    		// if either groundtruth or workflowResult is missing, skip validation on this MgmEvaluationFilesObj
    		if (skip) continue;
    		// otherwise continue further validation
    		log.debug("Validating groundtruth " + gtId + " and workflowResult " + wrId);

    		// validate that the groundtruth exists and is of the right format
    		// TODO detailed validation on csv/xml contents
    		Optional<PrimaryfileSupplement> groundtruthFile = supplementRepository.findById(gtId);
    		if (groundtruthFile == null) {
    			errors.add("Groundtruth file " + gtId + " does not exist.");
    		} else if (groundtruthFile != null && !groundtruthFile.get().getOriginalFilename().endsWith(mst.getGroundtruthFormat())) {
    			errors.add("Invalid file format for groundtruth " + gtId + ", please provide " + mst.getGroundtruthFormat() + " file.");
    		}

    		// validate the existence/status of the workflowResult and its associated primaryfile/output
    		Optional<WorkflowResult> wfr = wfRepo.findById(wrId);
    		if (wfr == null) {
    			errors.add("Workflow result " + wrId + " does not exist.");
    		} else {
    			Primaryfile primaryFile = pfRepository.findById(wfr.get().getPrimaryfileId()).orElse(null);
    			if (primaryFile == null) {
    				errors.add("Primaryfile for workflow result " + wrId + " does not exist.");
    			} else{
    				if (wfr.get().getStatus() != GalaxyJobState.COMPLETE) {
    					errors.add("Workflow Result " + wrId + " status is " + wfr.get().getStatus() + ", need to be COMPLETE");
    				}
    				String url = mediaService.getWorkflowResultOutputSymlinkUrl(wfr.get().getId());
    				if (url == null){
    					errors.add("No output file exists for Workflow Result " + wrId );
    				}
    			}
    		}
    	}
    	
    	return errors;
    }

    private List<String> validateRequiredParameters(ArrayList<MgmEvaluationParameterObj> inputParams, MgmScoringTool mst) {
        ArrayList<String> errors = new ArrayList<>();
        Set<MgmScoringParameter> mstParams = mst.getParameters();
        
        for (MgmScoringParameter p : mstParams) {
            log.debug("Validating parameter " + p.getName() + " for MST " + mst.getName());
            
            if (p.isRequired()){
                Optional<MgmEvaluationParameterObj> result = inputParams.stream().filter(obj -> obj.getId().equals(p.getId())).findFirst();
                if (result == null || result.isEmpty() || 
                	result.get().getValue() == null || result.get().getValue().isEmpty()) {
                    errors.add("Input parameter " + p.getName() + " is required but is missing.");
                }
            }
        }
        
        return errors;
    }

    @Override
    public MgmEvaluationValidationResponse process(MgmScoringTool mst, MgmEvaluationRequest request, AmpUser user) {
    	MgmEvaluationValidationResponse response = new MgmEvaluationValidationResponse();
    	List<MgmEvaluationTest> mgmEvaluationTests = new ArrayList<>();
    	int resultCount = 0;

    	// validate evaluation request and return errors if failed
    	log.info("Validating required Groundtruth-workflowResult files and parameters for evaluation test request ...");
    	List<String> errors = validate(request.getFiles(), request.getParameters(), mst);
    	if (!errors.isEmpty()) {
    		response.addErrors(errors);  
    		response.setSuccess(false);
    		response.setTestCount(mgmEvaluationTests.size());
    		response.setResultCount(resultCount);  
    		log.error("The evaluation request failed due to " + errors.size() + " validation errors, no test was created.");
    		return response;
    	}

    	// otherwise process each groundtruth-workflowResult evaluation test
    	for (MgmEvaluationFilesObj file : request.getFiles()) {
    		Long gtId = file.getGroundtruthFileId();
    		Long wrId = file.getWorkflowResultId();    		
    		PrimaryfileSupplement groundtruthFile = supplementRepository.findById(file.getGroundtruthFileId()).orElse(null);
    		WorkflowResult wfr = wfRepo.findById(file.getWorkflowResultId()).orElse(null);
    		log.info("Processing evaluation test for groundtruth " + gtId + " - workflowResult " + wrId);

    		MgmEvaluationTest mgmEvalTest = new MgmEvaluationTest();
    		
    		// serialize parameters into json string for DB storage, skip the test in case of exception
    		ArrayList<MgmEvaluationParameterObj> params = request.getParameters();
    		ObjectMapper mapper = new ObjectMapper();
    		try {
    			String newJsonData = mapper.writeValueAsString(params);
    			mgmEvalTest.setParameters(newJsonData);
    		} catch (Exception e) {
    			String error = "Failed to set test parameters for groundtruth " + gtId + " - workflowResult " + wrId;
    			response.addError(error);
    			log.error(error, e);
    			continue;
    		}
    		
    		// populate test info
    		mgmEvalTest.setCategory(mst.getCategory());
    		mgmEvalTest.setMst(mst);
    		mgmEvalTest.setSubmitter(user.getUsername());
    		mgmEvalTest.setWorkflowResult(wfr);
    		mgmEvalTest.setDateSubmitted(new Date());
    		mgmEvalTest.setStatus(TestStatus.RUNNING);
    		mgmEvalTest.setGroundtruthSupplement(groundtruthFile);
    		Primaryfile primaryFile = pfRepository.findById(wfr.getPrimaryfileId()).orElse(null);
    		mgmEvalTest.setPrimaryFile(primaryFile);
    		
    		// generate test command
    		List<String> cmd = new ArrayList<>();
    		cmd.add("amp_python.sif");
    		cmd.add(Paths.get(config.getMgmEvaluationScriptsRoot(), mst.getScriptPath()).toString());
    		cmd.add("-g");
    		String gtFilePath = Paths.get(config.getFileStorageRoot(), groundtruthFile.getPathname()).toString();
    		cmd.add(gtFilePath);
    		cmd.add("-m");
    		cmd.add(wfr.getOutputPath());
    		cmd.add("-o");
    		cmd.add(Paths.get(config.getMgmEvaluationResultsRoot()).toString());
    		
    		// process use-case
    		if (StringUtils.isNotBlank(mst.getUseCase())) {
    			cmd.add("--use-case");
    			cmd.add(mst.getUseCase());
    		}

    		// process parameters
    		for (MgmEvaluationParameterObj pp: params) {
    			// skip the param if its value is null or empty
    			if (pp.getValue() == null || pp.getValue().isEmpty()) continue;
    			
    			MgmScoringParameter obj = paramsRepo.findById(pp.getId()).orElse(null);
    			if (obj != null) {
    				pp.setShortName(obj.getShortName());
    				pp.setName(obj.getName());
    				cmd.add("--" + obj.getShortName());
    				cmd.add(String.join(",", pp.getValue()));
    			}
    		}
    		
    		// run test
    		log.info("Running MGM scoring tool with command: " + cmd.toString());
    		String result = runCMD((String[]) cmd.toArray(new String[0]));
    		
    		// record test result
    		if (result.startsWith("success:")) {
				String output = result.split("success:")[1];
				mgmEvalTest.setScorePath(output);
    			try {
    				JSONParser parser = new JSONParser();
    				if (!Files.exists(Paths.get(output))) {
    					Thread.sleep(1000);
    					log.trace("waiting for output..." + output); 
    				}
    				Object obj = parser.parse(new FileReader(output));
    				JSONObject jsonObject = (JSONObject)obj;
    				mgmEvalTest.setScores(jsonObject.toJSONString());
    				mgmEvalTest.setStatus(MgmEvaluationTest.TestStatus.SUCCESS);
    				resultCount++;
    				log.info("Successfully ran test for groundtruth " + gtId + " - workflowResult " + wrId + " with result: " + output);
    			} catch(Exception e) {
    				String errmsg = "failed to read output";
        			String error = "Successfully ran test but " + errmsg + " " + output + " for groundtruth " + gtId + " - workflowResult " + wrId;
    				mgmEvalTest.setStatus(MgmEvaluationTest.TestStatus.OUTPUT_ERROR);
        			mgmEvalTest.setMstErrorMsg(errmsg);
        			response.addError(error);    				
    				log.error(error, e);
    			} 
    		} else {
    			mgmEvalTest.setStatus(MgmEvaluationTest.TestStatus.RUNTIME_ERROR);
    			mgmEvalTest.setMstErrorMsg(result);
    			String error = "Failed to run test for groundtruth " + gtId + " - workflowResult " + wrId + ":\n" + result;
    			response.addError(error);    				
    			log.error(error);
    		}
    		
    		mgmEvaluationTests.add(mgmEvalTest);
    	}

    	// save tests and return response
    	mgmEvalRepo.saveAll(mgmEvaluationTests);
    	int testCount = mgmEvaluationTests.size();
    	response.setTestCount(testCount);
    	response.setResultCount(resultCount);
    	response.setSuccess(!response.hasErrors());
    	
    	// return response
    	log.info("Successfully created " + testCount + " evaluation tests and " + resultCount + " of them completed in success.");
    	return response;
    }

    private String runCMD(String[] cmd) {
		String result = "";

		try {
    		Process ps = Runtime.getRuntime().exec(cmd);
    		final int status = ps.waitFor();
    		String line = "";
    		BufferedReader reader = null;
    		StringBuilder builder = null;

    		// capture stdout of the command, which could include exceptions captured and pritned by the MST script,
    		// or success message if script completed in success
    		InputStream stdout = ps.getInputStream();
    		reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
			builder = new StringBuilder();
			while ( (line = reader.readLine()) != null) {
				builder.append(line);
				builder.append(System.getProperty("line.separator"));
			}
			result += builder.toString();	
			
    		// capture errors from executing the command, which would be exceptions not captured by the MST script,
    		// and the stacktrace would be sent by python running the script to stderr 
    		if (status != 0) {		
    			log.trace("cmd failed with status " + status);
    			reader = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
    			builder = new StringBuilder();
    			while ( (line = reader.readLine()) != null) {
    				builder.append(line);
    				builder.append(System.getProperty("line.separator"));
    			}
    			result += builder.toString();
    		}			
    	} catch (Exception e) {
    		result += ExceptionUtils.getStackTrace(e);    		
    	}
		
		return result;
    }

}
