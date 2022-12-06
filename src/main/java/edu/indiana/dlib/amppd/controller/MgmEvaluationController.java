package edu.indiana.dlib.amppd.controller;

import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.repository.MgmScoringToolRepository;
import edu.indiana.dlib.amppd.service.MgmEvaluationTestService;
import edu.indiana.dlib.amppd.web.MgmEvaluationRequest;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Slf4j
public class MgmEvaluationController {

    @Autowired
    MgmEvaluationTestService mgmTestService;

    @Autowired
    MgmScoringToolRepository mstRepo;
    @PostMapping(path = "/mgm-evaluation-test/new", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MgmEvaluationValidationResponse submitTestRequest(@RequestBody MgmEvaluationRequest request) {
        System.out.println(request.toString());
        System.out.println(request.getFiles().get(0).toString());
        MgmEvaluationValidationResponse response = new MgmEvaluationValidationResponse();
        Optional<MgmScoringTool> mst = mstRepo.findById(request.getMstId());
        if (mst.isPresent()) {
            return mgmTestService.validateGroundTruthFileFormat(request.getFiles(), mst.get());
        } else {
            response.addError("Mgm Scoring Tool is required.");
        }
        return response;
    }

    @PostMapping(path = "/mgm-evaluation-test/test")
    public String TestRequest() {
        return "hello world";
    }
}
