package edu.indiana.dlib.amppd.service;

import java.nio.file.Path;
import java.util.List;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.MgmEvaluationTest;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.web.MgmEvaluationRequest;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;


/**
 * Service for processing MGM evaluation test and related data.
 * @author yingfeng
 */
public interface MgmEvaluationService {

    public MgmEvaluationTestResponse getMgmEvaluationTests(MgmEvaluationSearchQuery query);

    public MgmEvaluationValidationResponse process(MgmScoringTool mst, MgmEvaluationRequest request, AmpUser user);
    
    /**
     * Delete the output file associated with the specified MgmEvaluationTest.
     * @param met the specified MgmEvaluationTest
     * @return the path of the output file deleted or null if not existing
     */
    public Path deleteEvaluationOutput(MgmEvaluationTest met);

    /**
     * Delete all MgmEvaluationTest output files associated with the specified supplement if applicable, i.e.
     * if the supplement is a groundtruth associated with MgmEvaluationTests.
     * @param supplement the specified supplement
     * @return the list of MgmEvaluationTests associated with the supplement 
     */
    public List<MgmEvaluationTest> deleteEvaluationOutputs(Supplement supplement);

    /**
     * Delete all MgmEvaluationTest output files associated with all applicable PrimaryfileSupplements under the specified Dataentity.
     * @param entity the specified Dataentity
     * @return the list of MgmEvaluationTests with outputs deleted
     */
    public List<MgmEvaluationTest> deleteEvaluationOutputs(Dataentity dataentity);

}
