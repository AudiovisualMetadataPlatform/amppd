package edu.indiana.dlib.amppd.service.impl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
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
        log.info("Successfully retrieved " + response.getTotalResults() + " WorkflowResults for search  query " + query);
        return response;
    }

    private List<String> validate(ArrayList<MgmEvaluationFilesObj> files, ArrayList<MgmEvaluationParameterObj> inputParams, MgmScoringTool mst){
        log.info("Validating required files and params");
        ArrayList<String> errors = new ArrayList<>();
        if (files.size() <= 0) {
            errors.add("No Primary and Groundtruth files selected.");
        }
        errors.addAll(validateFiles(files, mst));
        errors.addAll(validateRequiredParameters(inputParams, mst));
        return errors;
    }


    private List<String> validateFiles(ArrayList<MgmEvaluationFilesObj> files,MgmScoringTool mst) {
        ArrayList<String> errors = new ArrayList<>();
        if (files.size() > 0) {
            for (MgmEvaluationFilesObj file : files) {

                if(file.getGroundtruthFileId() == null) {
                    errors.add("Groundtruth file is required.");
                } else {
                    log.info("Validating Groundtruth files format");
                    Optional<PrimaryfileSupplement> groundtruthFile = supplementRepository.findById(file.getGroundtruthFileId());
                    if (groundtruthFile == null) {
                        errors.add("Groundtruth file does not exist.");
                    } else if (groundtruthFile != null && !groundtruthFile.get().getOriginalFilename().endsWith(mst.getGroundtruthFormat())) {
                        errors.add("Invalid groundtruth file format for " + groundtruthFile.get().getName() + ". Please provide " + mst.getGroundtruthFormat() + " file.");
                    }
                }
                if(file.getWorkflowResultId() == null) {
                    errors.add("Workflow is required for evaluation test.");
                }else {
                    log.info("Validating workflow existed in request.");
                    Optional<WorkflowResult> wfr = wfRepo.findById(file.getWorkflowResultId());
                    if (wfr == null) {
                        errors.add("Workflow does not exist.");
                    } else if (wfr != null) {
                        log.info("Validating primary file in WF");
                        Primaryfile primaryFile = pfRepository.findById(wfr.get().getPrimaryfileId()).orElse(null);
                        if (primaryFile == null) {
                            errors.add("Primary File is required for evaluation test.");
                        } else{
                            log.info("Rimsha test ----> " + wfr.get().getStatus());
                            if(!wfr.get().getStatus().toString().contains("COMPLETE")) {
                                errors.add("Workflow Result status should be complete. " + wfr.get().getStatus()  +" status for primary file: " + primaryFile.getName());
                            }
                            String url = mediaService.getWorkflowResultOutputSymlinkUrl(wfr.get().getId());
                            if(url == null){
                                errors.add("No mgm output file existed.");
                            }
                        }
                    }

                }
            }
        }
        return errors;
    }

    private List<String> validateRequiredParameters(ArrayList<MgmEvaluationParameterObj> inputParams, MgmScoringTool mst) {
        log.info("Validating all required parameters filled");
        ArrayList<String> errors = new ArrayList<>();
        Set<MgmScoringParameter> mstParams = mst.getParameters();
        for (MgmScoringParameter p : mstParams) {
            if(p.isRequired() == true){
                Optional<MgmEvaluationParameterObj> result = inputParams.stream().filter(obj -> obj.getId().equals(p.getId())).findFirst();
                if(result == null || result.isEmpty() || result.get().getValue() == null || result.get().getValue().size() <= 0) {
                    errors.add("Input parameter " + p.getName() + " is required.");
                }
            }
        }
        return errors;
    }

    @Override
    public MgmEvaluationValidationResponse process(MgmScoringTool mst, MgmEvaluationRequest request, AmpUser user) {
        MgmEvaluationValidationResponse response = new MgmEvaluationValidationResponse();
        log.info("Validating request");
        List<String> errors = validate(request.getFiles(), request.getParameters(), mst);
        List<MgmEvaluationTest> mgmEvaluationTests = new ArrayList<>();
        response.addErrors(errors);
        if(!response.hasErrors()){
            for (MgmEvaluationFilesObj file : request.getFiles()) {
                PrimaryfileSupplement groundtruthFile = supplementRepository.findById(file.getGroundtruthFileId()).orElse(null);
                WorkflowResult wfr = wfRepo.findById(file.getWorkflowResultId()).orElse(null);
                log.info("Processing file: "+ groundtruthFile.getName() + " with WF " + wfr.getWorkflowName());
                MgmEvaluationTest mgmEvalTest = new MgmEvaluationTest();
                mgmEvalTest.setCategory(mst.getCategory());
                mgmEvalTest.setMst(mst);
                mgmEvalTest.setSubmitter(user.getUsername());
                ArrayList<MgmEvaluationParameterObj> params = request.getParameters();
                log.info("Preparing command");
                List<String> cmd = new ArrayList<>();
                cmd.add("amp_python.sif");
                cmd.add(Paths.get(config.getMgmEvaluationScriptsRoot(), mst.getScriptPath()).toString());
                cmd.add("-g");
//                String gtFilePath = config.getFileStorageRoot() + File.separator + groundtruthFile.getPathname();
                String gtFilePath = Paths.get(config.getFileStorageRoot(), groundtruthFile.getPathname()).toString();
                cmd.add(gtFilePath);
                cmd.add("-m");
                cmd.add(wfr.getOutputPath());
                cmd.add("-o");
                cmd.add(Paths.get(config.getDropboxRoot(), "mgm_scoring_tools").toString());
                if (mst.getUseCase() != null && mst.getUseCase() != "") {
                    cmd.add("--use-case");
                    cmd.add(mst.getUseCase());
                }
                if(!params.isEmpty()) {
                    for(MgmEvaluationParameterObj pp: params) {
                        MgmScoringParameter obj = paramsRepo.findById(pp.getId()).orElse(null);
                        if (obj != null) {
                            pp.setShortName(obj.getShortName());
                            pp.setName(obj.getName());
                            cmd.add("--" + obj.getShortName());
                            cmd.add(String.join(",", pp.getValue()));
                        }
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        String newJsonData = mapper.writeValueAsString(params);
                        mgmEvalTest.setParameters(newJsonData);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
                mgmEvalTest.setWorkflowResult(wfr);
                mgmEvalTest.setDateSubmitted(new Date());
                mgmEvalTest.setStatus(MgmEvaluationTest.TestStatus.RUNNING);
                mgmEvalTest.setGroundtruthSupplement(groundtruthFile);
                Primaryfile primaryFile = pfRepository.findById(wfr.getPrimaryfileId()).orElse(null);
                mgmEvalTest.setPrimaryFile(primaryFile);
                log.info("Command: " + cmd.toString());
                String result = runCMD((String[]) cmd.toArray(new String[0]));
                try {
                    if(result.startsWith("success:")) {
                        log.info("command run successfully.");
                        String output = result.split("success:")[1];
                        JSONParser parser = new JSONParser();
                        Object obj = parser.parse(new FileReader(output));
                        JSONObject jsonObject = (JSONObject)obj;
                        mgmEvalTest.setScores(jsonObject.toJSONString());
                        mgmEvalTest.setStatus(MgmEvaluationTest.TestStatus.SUCCESS);
                        mgmEvalTest.setScorePath(output);
                    } else {
                        mgmEvalTest.setStatus(MgmEvaluationTest.TestStatus.RUNTIME_ERROR);
                        mgmEvalTest.setMstErrorMsg(result);
                        errors.add("RUNTIME ERROR: Failed to process.");
                        log.info("File "+ groundtruthFile.getName() + " with WF " + wfr.getWorkflowName() + " failed with error msg: "+ result);
                    }
                } catch(IOException e){
                    log.error("Exception in reading output "+ e.toString());
                } catch (ParseException e) {
                    log.error("Exception in reading output file "+ e.toString());
                }
                mgmEvaluationTests.add(mgmEvalTest);
            }
            mgmEvalRepo.saveAll(mgmEvaluationTests);
            response.setMgmEvaluationTestCount(mgmEvaluationTests.size());
        }
        if(response.hasErrors()){
            response.setSuccess(false);
        } else {
            response.setSuccess(true);
        }
        return response;
    }

    private String runCMD(String[] cmd) {
        try {
            Process ps = Runtime.getRuntime().exec(cmd);
            InputStream stdout = ps.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
            String result = "";
            String line;
            try{
                log.info("Checking script output");
                while((line = reader.readLine()) != null){
                    result += line;
                }
                return result;
            } catch(IOException e){
                log.error("Exception in reading output "+ e.toString());
            }
        } catch (IOException e) {
            log.error("Exception in running cmd "+ e.toString());
            log.error(cmd.toString());
        }
        return null;
    }

}
