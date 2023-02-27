package edu.indiana.dlib.amppd.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.*;
import edu.indiana.dlib.amppd.repository.*;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.web.*;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
        ArrayList<String> errors = new ArrayList<>();
        if (files.size() <= 0) {
            errors.add("No Primary and Groundtruth files selected.");
        }
        errors.addAll(validateFiles(files, mst));
        errors.addAll(validateRequiredParameters(inputParams, mst));
        return errors;
    }


    private List<String> validateFiles(ArrayList<MgmEvaluationFilesObj> files,MgmScoringTool mst) {
        log.info("Validating Groundtruth files format");
        ArrayList<String> errors = new ArrayList<>();
        if (files.size() > 0) {
            for (MgmEvaluationFilesObj file : files) {
                if(file.getGroundtruthFileId() == null) {
                    errors.add("Groundtruth file is required.");
                } else {
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
                    Optional<WorkflowResult> wfr = wfRepo.findById(file.getWorkflowResultId());
                    if (wfr == null) {
                        errors.add("Workflow does not exist.");
                    } else if (wfr != null) {
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
        log.info("Validating if all required parameters");
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
        List<String> errors = validate(request.getFiles(), request.getParameters(), mst);
        List<MgmEvaluationTest> mgmEvaluationTests = new ArrayList<>();
        response.addErrors(errors);
        if(!response.hasErrors()){
            for (MgmEvaluationFilesObj file : request.getFiles()) {
                MgmEvaluationTest mgmEvalTest = new MgmEvaluationTest();
                mgmEvalTest.setCategory(mst.getCategory());
                mgmEvalTest.setMst(mst);
                mgmEvalTest.setSubmitter(user.getUsername());
                ArrayList<MgmEvaluationParameterObj> params = request.getParameters();
                PrimaryfileSupplement groundtruthFile = supplementRepository.findById(file.getGroundtruthFileId()).orElse(null);
                WorkflowResult wfr = wfRepo.findById(file.getWorkflowResultId()).orElse(null);
                List<String> cmd = new ArrayList<>();
                cmd.add("amp_python.sif");
                cmd.add(config.getMgmEvaluationScriptsRoot() + File.separator +  mst.getScriptPath());
                cmd.add("-g");
                String gtFilePath = config.getFileStorageRoot() + File.separator + groundtruthFile.getPathname();
                cmd.add(gtFilePath);
                cmd.add("-m");
                cmd.add(wfr.getOutputPath());
                cmd.add("-o");
                cmd.add(config.getDropboxRoot() + File.separator + "mgm_scoring_tools");
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
                log.info(cmd.toString());

                String result = runCMD((String[]) cmd.toArray(new String[0]));

                log.info(result);
                try {
                    if(result.startsWith("success:")) {
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
                System.out.println("Checking output");
                while((line = reader.readLine()) != null){
                    log.info("line---> " + line);
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
