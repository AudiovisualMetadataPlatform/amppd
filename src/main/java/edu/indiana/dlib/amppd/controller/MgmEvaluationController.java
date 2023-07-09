package edu.indiana.dlib.amppd.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.repository.MgmScoringToolRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.MgmEvaluationService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.web.MgmEvaluationFilesObj;
import edu.indiana.dlib.amppd.web.MgmEvaluationRequest;
import edu.indiana.dlib.amppd.web.MgmEvaluationSearchQuery;
import edu.indiana.dlib.amppd.web.MgmEvaluationTestResponse;
import edu.indiana.dlib.amppd.web.MgmEvaluationValidationResponse;
import lombok.extern.slf4j.Slf4j;
@RestController
@Slf4j
public class MgmEvaluationController {
    @Autowired
    private MgmEvaluationService mgmEvalService;

    @Autowired
    MgmScoringToolRepository mstRepo;

    @Autowired
    private AmpUserService ampUserService;

    @Autowired
    private PermissionService permissionService;

    
    @PostMapping(path = "/mgm-evaluation-test/new", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MgmEvaluationValidationResponse submitTestRequest(@RequestBody MgmEvaluationRequest request) {
		// get accessible units for Create MgmEvaluationTest
		// if none returned, access denied exception will be thrown;
		// otherwise if accessibleUnits is null, i.e. user is admin, no AC prefilter is needed; 
		// otherwise, each MgmEvaluationFilesObj in the request is limited within the accessible units
		Set<Long> acUnitIds = permissionService.getAccessibleUnitIds(ActionType.Create, TargetType.MgmEvaluationTest);
		List<MgmEvaluationFilesObj> files = request.getFiles();
		if (acUnitIds != null) {
			for (MgmEvaluationFilesObj fobj : files ) {
				Long id = fobj.getWorkflowResultId();
				Long unitId = permissionService.getAcUnitId(id, WorkflowResult.class);
				// Note: We could skip the current test instead of exception upon encountering invalid WorkflowResult-Groundtruth.
				// In practice, exception shouldn't happen if the client sends legitimate WorkflowResult IDs from prior response.
				if (!acUnitIds.contains(unitId)) {
					throw new AccessDeniedException("The current user cannot Create MgmEvaluationTest for WorkflowResult " + id + " in unit " + unitId);			
				}
			}
		}

		log.info("Submitting Mgm Evaluation Tests: " + request);
        AmpUser ampUser = ampUserService.getCurrentUser();
        MgmEvaluationValidationResponse response = new MgmEvaluationValidationResponse();
        Long mstId = request.getMstId();
        MgmScoringTool mst = mstRepo.findById(mstId).orElseThrow(() -> new StorageException("Mgm scoring tool <" + request.getMstId() + "> does not exist!"));
        if (mst != null) {
            response = mgmEvalService.process(mst, request, ampUser);
        } else {
            response.addError("Failed to process evaluation request: Mgm Scoring Tool " + mstId + " does not exist.");
        }
        return response;
    }

    @PostMapping(path = "/mgm-evaluation-test/test")
    public String TestRequest() {
        return "hello world";
    }

    /**
     * Retrieve a list of all mgm evaluation tests satisfying the given query.
     * @param query the search query for mgm evaluation test
     * @return the MgmEvaluationTestResponse containing the list of queried mgm evaluation test
     */
    @PostMapping(path = "/mgm-evaluation-test/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MgmEvaluationTestResponse filterMgmEvaluationTest(@RequestBody MgmEvaluationSearchQuery query){
		// AC prefilter on MgmEvaluationSearchQuery to restrict unit filters to only accessible ones by current user
		Set<Long> accessibleUnits = permissionService.prefilter(query);
    	
        log.info("Retrieving MgmEvaluationTest for query ...");
        MgmEvaluationTestResponse response =  mgmEvalService.getMgmEvaluationTests(query);

        // AC postfilter on MgmEvaluationTestResponse to restrict MgmEvaluationTestFilters.units to only accessible ones by current user
		permissionService.postfilter(response, accessibleUnits);		

        return response;
    }
}
