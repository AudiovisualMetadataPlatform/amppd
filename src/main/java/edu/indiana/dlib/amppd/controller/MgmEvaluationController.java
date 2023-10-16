package edu.indiana.dlib.amppd.controller;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.MgmScoringTool;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.projection.MgmEvaluationTestDetail;
import edu.indiana.dlib.amppd.repository.MgmEvaluationTestRepository;
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
    MgmScoringToolRepository mstRepo;

    @Autowired
    MgmEvaluationTestRepository metRepo;

    @Autowired
    private MgmEvaluationService mgmEvalService;

    @Autowired
    private AmpUserService ampUserService;

    @Autowired
    private PermissionService permissionService;

    
	/**
	 * Get MGM evaluation tests with the given list of IDs.
	 * @param ids the given MGM evaluation test IDs
	 * @return the list of MGM evaluation tests found
	 */
	@GetMapping(path = "/mgmEvaluationTests")
	public List<MgmEvaluationTestDetail> findByIds(@RequestParam List<Long> ids) {
		// get accessible units for Read MgmEvaluationTest, if none, access denied exception will be thrown
		Set<Long> acUnitIds = permissionService.getAccessibleUnitIds(ActionType.Read, TargetType.MgmEvaluationTest);

		// otherwise if acUnitIds is null, i.e. user is admin, then no AC prefilter is needed;  
		// otherwise apply AC prefilter to query criteria	
		List<MgmEvaluationTestDetail> mets = acUnitIds == null ?
				metRepo.findByIdIn(ids) :
				metRepo.findByIdInAndWorkflowResultUnitIdIn(ids, acUnitIds);

		log.info("Successfully found " + mets.size() + " MGM evaluation tests for the given " + ids.size() + " IDs.");		
		return mets;
	}

    @PostMapping(path = "/mgmEvaluationTests/create", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public MgmEvaluationValidationResponse createMgmEvalTest(@RequestBody MgmEvaluationRequest request) {
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

    /**
     * Retrieve a list of all mgm evaluation tests satisfying the given query.
     * @param query the search query for mgm evaluation test
     * @return the MgmEvaluationTestResponse containing the list of queried mgm evaluation test
     */
    @PostMapping(path = "/mgmEvaluationTests/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
