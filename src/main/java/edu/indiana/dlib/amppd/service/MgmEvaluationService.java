package edu.indiana.dlib.amppd.service;

import java.util.List;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.web.MgmEvaluationRequest;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;

public interface MgmEvaluationService {

    public MgmEvaluationTestResponse getMgmEvaluationTests(MgmEvaluationSearchQuery query);

    public MgmEvaluationValidationResponse process(MgmScoringTool mst, MgmEvaluationRequest request, AmpUser user);
    
    /**
     * Delete MgmEvaluationTest output files associated with the specified supplement if applicable, i.e.
     * if the supplement is a groundtruth associated with MgmEvaluationTests.
     * @param supplement the specified supplement
     * @return the list of MgmEvaluationTests associated with the supplement 
     */
    public List<MgmEvaluationTest> deleteEvaluationOutputs(Supplement supplement);
    
}
