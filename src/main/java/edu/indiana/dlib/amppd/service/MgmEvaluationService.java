package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;

import java.util.ArrayList;
import java.util.Map;

public interface MgmEvaluationService {

    public MgmEvaluationTestResponse getMgmEvaluationTests(MgmEvaluationSearchQuery query);

    MgmEvaluationValidationResponse process(MgmScoringTool mst, Long categoryId, ArrayList<Map> files, ArrayList<Map> parameters);
}
