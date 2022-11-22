package edu.indiana.dlib.amppd.service.impl;

import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.repository.SupplementRepository;
import edu.indiana.dlib.amppd.service.MgmEvaluationValidationService;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Map;

@Service
public class MgmEvaluationValidationServiceImpl implements MgmEvaluationValidationService {

    @Autowired
    SupplementRepository supplementRepo;

    @Override
    public MgmEvaluationValidationResponse validateGroundTruthFileFormat(ArrayList<Map> files, MgmScoringTool mst) {
        MgmEvaluationValidationResponse response = null;
        for(Map<String, Long> file: files) {
            String groundtruth_filename = String.valueOf(file.get("groundtruth_filename"));
            if(!groundtruth_filename.endsWith(mst.getGroundtruthFormat())) {
                response.addError("Invalid groundtruth file format for " + groundtruth_filename + ". Please provide " + mst.getGroundtruthFormat() + " file.");
            }
        }
        return response;
    }

    @Override
    public MgmEvaluationValidationResponse validateRequiredParameters(ArrayList<Map> parameters) {
        MgmEvaluationValidationResponse response = null;
        return response;
    }

    @Override
    public MgmEvaluationValidationResponse validateFiles(ArrayList<Map> files) {
        MgmEvaluationValidationResponse response = null;
        return response;
    }
}
