package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;

import java.util.ArrayList;
import java.util.Map;

public interface MgmEvaluationTestService {

    MgmEvaluationValidationResponse process(MgmScoringTool mst, Long categoryId, ArrayList<Map> files, ArrayList<Map> parameters);
    MgmEvaluationValidationResponse validateGroundTruthFileFormat(MgmEvaluationValidationResponse response, ArrayList<Map> files, MgmScoringTool mst);
    MgmEvaluationValidationResponse validateRequiredParameters(ArrayList<Map> parameters, MgmScoringTool mst);
    MgmEvaluationValidationResponse validateFiles(ArrayList<Map> files, MgmScoringTool mst);
}
