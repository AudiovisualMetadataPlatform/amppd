package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;

import java.util.ArrayList;
import java.util.Map;

public interface MgmEvaluationValidationService {
    MgmEvaluationValidationResponse validateGroundTruthFileFormat(ArrayList<Map> files, MgmScoringTool mst);
    MgmEvaluationValidationResponse validateRequiredParameters(ArrayList<Map> parameters, MgmScoringTool mst);
    MgmEvaluationValidationResponse validateFiles(ArrayList<Map> files, MgmScoringTool mst);
}
