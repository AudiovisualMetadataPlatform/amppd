package edu.indiana.dlib.amppd.controller;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.repository.MgmScoringToolRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.web.MgmEvaluationRequest;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
@Slf4j
public class MgmEvaluationController {
    @Autowired
    private MgmEvaluationService mgmEvalService;

    @Autowired
    MgmScoringToolRepository mstRepo;

    @Autowired
    private AmpUserService ampUserService;

    @PostMapping(path = "/mgm-evaluation-test/new", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MgmEvaluationValidationResponse submitTestRequest(@RequestBody MgmEvaluationRequest request) {
        AmpUser ampUser = ampUserService.getCurrentUser();
        MgmEvaluationValidationResponse response = new MgmEvaluationValidationResponse();
        MgmScoringTool mst = mstRepo.findById(request.getMstId()).orElseThrow(() -> new StorageException("Mgm scoring tool <" + request.getMstId() + "> does not exist!"));
        if (mst != null) {
            return mgmEvalService.process(mst, request, ampUser);
        } else {
            response.addError("Mgm Scoring Tool is required.");
        }
        return response;
    }

    @PostMapping(path = "/mgm-evaluation-test/test")
    public String TestRequest() {
        return "hello world";
    }

    /**
     * Get a list of all mgm evaluation tests satisfying the given query.
     * @param query the search query for mgm evaluation test
     * @return the MgmEvaluationTestResponse containing the list of queried mgm evaluation test
     */
    @PostMapping(path = "/mgm-evaluation-test/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MgmEvaluationTestResponse getMgmEvaluationTest(@RequestBody MgmEvaluationSearchQuery query){
        log.info("Retrieving MgmEvaluationTest for query ...");
        return mgmEvalService.getMgmEvaluationTests(query);
    }
}
