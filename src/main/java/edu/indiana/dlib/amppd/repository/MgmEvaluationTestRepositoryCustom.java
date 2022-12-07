package edu.indiana.dlib.amppd.repository;

import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;

public interface MgmEvaluationTestRepositoryCustom {
    public MgmEvaluationTestResponse findByQuery(MgmEvaluationSearchQuery mesq);
}
