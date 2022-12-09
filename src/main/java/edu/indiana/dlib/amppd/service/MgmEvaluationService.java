package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.web.MgmEvaluationRequest;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;

public interface MgmEvaluationService {

    public MgmEvaluationTestResponse getMgmEvaluationTests(MgmEvaluationSearchQuery query);

    MgmEvaluationValidationResponse process(MgmScoringTool mst, MgmEvaluationRequest request, AmpUser user);
}
