package edu.indiana.dlib.amppd.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.indiana.dlib.amppd.model.*;
import edu.indiana.dlib.amppd.repository.*;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.web.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                if(result == null || result.isEmpty() || result.get().getValue() == null || result.get().getValue() == "") {
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
                if(!params.isEmpty()) {
                    for(MgmEvaluationParameterObj pp: params) {
                        MgmScoringParameter obj = paramsRepo.findById(pp.getId()).orElse(null);
                        if (obj != null) {
                            pp.setShortName(obj.getShortName());
                            pp.setName(obj.getName());
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
                WorkflowResult wfr = wfRepo.findById(file.getWorkflowResultId()).orElse(null);
                mgmEvalTest.setWorkflowResult(wfr);
                mgmEvalTest.setDateSubmitted(new Date());
                mgmEvalTest.setStatus(MgmEvaluationTest.TestStatus.RUNNING);
                PrimaryfileSupplement groundtruthFile = supplementRepository.findById(file.getGroundtruthFileId()).orElse(null);
                mgmEvalTest.setGroundtruthSupplement(groundtruthFile);
                Primaryfile primaryFile = pfRepository.findById(wfr.getPrimaryfileId()).orElse(null);
                mgmEvalTest.setPrimaryFile(primaryFile);
                mgmEvaluationTests.add(mgmEvalTest);
            }
        }

        if(response.hasErrors()){
            response.setSuccess(false);
        } else {
            mgmEvalRepo.saveAll(mgmEvaluationTests);
            response.setMgmEvaluationTestCount(mgmEvaluationTests.size());
            response.setSuccess(true);
        }
        return response;
    }
}
