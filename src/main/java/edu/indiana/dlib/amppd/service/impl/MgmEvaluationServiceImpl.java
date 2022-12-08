package edu.indiana.dlib.amppd.service.impl;

import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.repository.MgmEvaluationTestRepository;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.List;

@Service
@Slf4j
public class MgmEvaluationServiceImpl implements MgmEvaluationService {
    @Autowired
    private MgmEvaluationTestRepository mgmEvalRepo;

    @Override
    public MgmEvaluationTestResponse getMgmEvaluationTests(MgmEvaluationSearchQuery query) {
        MgmEvaluationTestResponse response = mgmEvalRepo.findByQuery(query);
        log.info("Successfully retrieved " + response.getTotalResults() + " WorkflowResults for search  query " + query);
        return response;
    }

    private List<String> validate(ArrayList<Map> files, ArrayList<Map> inputParams, MgmScoringTool mst){
        ArrayList<String> errors = new ArrayList<>();
        if (files.size() <= 0) {
            errors.add("No Primary and Groundtruth files selected.");
        }
        errors.addAll(validateFiles(files, mst));
        errors.addAll(validateGroundTruthFileFormat(files, mst));
        errors.addAll(validateRequiredParameters(inputParams, mst));
        return errors;
    }


    private List<String> validateGroundTruthFileFormat(ArrayList<Map> files,MgmScoringTool mst) {
        log.info("Validating Groundtruth files format");
        ArrayList<String> errors = new ArrayList<>();
        if (files.size() > 0) {
            for (Map<String, Map<String, String>> file : files) {
                Map<String, String> groundtruth = file.get("groundtruthFile");
                if (groundtruth != null){
                    if (!groundtruth.get("original_filename").endsWith(mst.getGroundtruthFormat())) {
                        errors.add("Invalid groundtruth file format for " + groundtruth.get("name") + ". Please provide " + mst.getGroundtruthFormat() + " file.");
                    }
                }
            }
        }
        return errors;
    }

    private List<String> validateRequiredParameters(ArrayList<Map> inputParams, MgmScoringTool mst) {
        log.info("Validating if all required parameters");
        ArrayList<String> errors = new ArrayList<>();
        Set<MgmScoringParameter> mstParams = mst.getParameters();
        for (MgmScoringParameter p : mstParams) {
            if(p.isRequired() == true){
                System.out.println("Rimsha helloooo");
                System.out.println(p.getId());
                Optional<Map> result = inputParams.stream().filter(obj -> obj.get("id") == p.getId()).findFirst();
                if(result == null || result.isEmpty() || result.get().get("value") == null || result.get().get("value") == "") {
                    errors.add("Input parameter " + p.getName() + " is required.");
                }
            }
        }
        return errors;
    }
    private List<String> validateFiles(ArrayList<Map> files, MgmScoringTool mst) {
        log.info("Validating if all groundtruth and primary files existed");
        ArrayList<String> errors = new ArrayList<>();
        for (Map<String, Map<String, String>> file : files) {
            if(file.get("groundtruthFile") == null || file.get("primaryFile") == null){
                errors.add("Both primary and groundtruth files required.");
            }
        }
        return errors;
    }

    @Override
    public MgmEvaluationValidationResponse process(MgmScoringTool mst, Long categoryId, ArrayList<Map> files, ArrayList<Map> parameters) {
        MgmEvaluationValidationResponse response = new MgmEvaluationValidationResponse();
        List<String> errors = validate(files, parameters, mst);
        if(errors.size() > 0){
            response.addErrors(errors);
        }

        return response;
    }
}
