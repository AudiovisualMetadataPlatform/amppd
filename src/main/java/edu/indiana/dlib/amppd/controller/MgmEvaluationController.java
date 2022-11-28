package edu.indiana.dlib.amppd.controller;

import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.repository.MgmScoringToolRepository;
import edu.indiana.dlib.amppd.service.MgmEvaluationValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import edu.indiana.dlib.amppd.web.MgmEvaluationRequest;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;
import java.util.Optional;

@RestController
@Slf4j
public class MgmEvaluationController {

    @Autowired
    MgmEvaluationValidationService validationService;

    @Autowired
    MgmScoringToolRepository mstRepo;
    @PostMapping(path = "/mgm-evaluation-test/new")
    public MgmEvaluationValidationResponse submitTestRequest(MgmEvaluationRequest request) {
        Optional<MgmScoringTool> mst = mstRepo.findById(request.getMstId());
        if (mst.isPresent()) {
            return validationService.validateGroundTruthFileFormat(request.getFiles(), mst.get());
        }
        return null;
    }
}
