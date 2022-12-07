package edu.indiana.dlib.amppd.controller;

import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
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
