package edu.indiana.dlib.amppd.service.impl;

import edu.indiana.dlib.amppd.model.MgmScoringParameter;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.service.MgmEvaluationValidationService;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class MgmEvaluationValidationServiceImpl implements MgmEvaluationValidationService {
    @Override
    public MgmEvaluationValidationResponse validateGroundTruthFileFormat(ArrayList<Map> files, MgmScoringTool mst) {
        MgmEvaluationValidationResponse response = null;
        for(Map<String, String> file: files) {
            String groundtruth_filename = String.valueOf(file.get("groundtruth_filename"));
            if(!groundtruth_filename.endsWith(mst.getGroundtruthFormat())) {
                response.addError("Invalid groundtruth file format for " + groundtruth_filename + ". Please provide " + mst.getGroundtruthFormat() + " file.");
            }
        }
        return response;
    }

    @Override
    public MgmEvaluationValidationResponse validateRequiredParameters(ArrayList<Map> inputParams, MgmScoringTool mst) {
        MgmEvaluationValidationResponse response = null;
        Set<MgmScoringParameter> mstParams = mst.getParameters();
        for (MgmScoringParameter p : mstParams) {
            if(p.isRequired() == true){
                Optional<Map> result = inputParams.stream().filter(obj -> obj.get("id") == p.getId()).findFirst();
                if(result == null || result.isEmpty() || result.get().get("value") == null || result.get().get("value") == "") {
                    response.addError("Input parameter " + p.getName() + " is required.");
                }
            }
        }
        return response;
    }

    @Override
    public MgmEvaluationValidationResponse validateFiles(ArrayList<Map> files, MgmScoringTool mst) {
        MgmEvaluationValidationResponse response = null;
        for(Map<String, String> file: files) {
            System.out.println(file);
        }
//        for(Map<String, String> file: files) {
//            if(file.get("groundtruth"))
//            String groundtruth_filename = String.valueOf(file.get("groundtruth_filename"));
//            if(!groundtruth_filename.endsWith(mst.getGroundtruthFormat())) {
//                response.addError("Invalid groundtruth file format for " + groundtruth_filename + ". Please provide " + mst.getGroundtruthFormat() + " file.");
//            }
//        }
        return response;
    }
}
