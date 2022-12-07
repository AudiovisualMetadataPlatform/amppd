package edu.indiana.dlib.amppd.service;

import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;

public interface MgmEvaluationService {

    public MgmEvaluationTestResponse getMgmEvaluationTests(MgmEvaluationSearchQuery query);
}
