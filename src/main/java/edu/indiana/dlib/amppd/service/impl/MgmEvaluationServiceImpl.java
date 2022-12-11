package edu.indiana.dlib.amppd.service.impl;

import edu.indiana.dlib.amppd.model.*;
import edu.indiana.dlib.amppd.repository.MgmEvaluationTestRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
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
                MgmEvaluationGroundtruthObj groundtruth = file.getGroundtruthFile();
                MgmEvaluationPrimaryFileObj primaryFile = file.getPrimaryFile();
                if(groundtruth.getId() == null || primaryFile.getId() == null) {
                    errors.add("Both primary and groundtruth files required.");
                } else if (groundtruth.getId() != null){
                    if (groundtruth.getOriginalFilename() != null && !groundtruth.getOriginalFilename().endsWith(mst.getGroundtruthFormat())) {
                        errors.add("Invalid groundtruth file format for " + groundtruth.getName() + ". Please provide " + mst.getGroundtruthFormat() + " file.");
                    } else if (groundtruth.getOriginalFilename() == null) {
                        Optional<PrimaryfileSupplement> groundtruthFile = supplementRepository.findById(groundtruth.getId());
                        if(groundtruthFile.isEmpty()){
                            errors.add("Groundtruth file "+ groundtruth.getName() +" does not exist.");
                        } else if (groundtruthFile.get().getOriginalFilename() != null && !groundtruthFile.get().getOriginalFilename().endsWith(mst.getGroundtruthFormat())) {
                            errors.add("Invalid groundtruth file format for " + groundtruth.getName() + ". Please provide " + mst.getGroundtruthFormat() + " file.");
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
                Optional<MgmEvaluationParameterObj> result = inputParams.stream().filter(obj -> obj.getId() == p.getId()).findFirst();
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
                
                System.out.println(request.getParameters().toString());
//                mgmEvalTest.setParameters(String.valueOf(parameters));
                MgmEvaluationGroundtruthObj groundtruth = file.getGroundtruthFile();
                mgmEvalTest.setDateSubmitted(new Date());
                mgmEvalTest.setStatus(MgmEvaluationTest.TestStatus.RUNNING);
                PrimaryfileSupplement groundtruthFile = supplementRepository.findById(groundtruth.getId()).orElse(null);
                mgmEvalTest.setGroundtruthSupplement(groundtruthFile);
                WorkflowResult wfr = wfRepo.findById(file.getWorkflowId()).orElse(null);
                if(wfr != null){
                    mgmEvalTest.setWorkflowResult(wfr);
                    mgmEvaluationTests.add(mgmEvalTest);
                } else {
                    response.addError("Workflow is required for evaluation test.");
                }
            }
        }

        if(response.hasErrors()){
            response.setSuccess(false);
        } else {
            mgmEvalRepo.saveAll(mgmEvaluationTests);
            response.setMgmEvaluationTest(mgmEvaluationTests);
            response.setSuccess(true);
        }
        return response;
    }
}
