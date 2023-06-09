package edu.indiana.dlib.amppd.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.dto.ItemInfo;
import edu.indiana.dlib.amppd.model.dto.PrimaryfileInfo;
import edu.indiana.dlib.amppd.model.projection.PrimaryfileIdInfo;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.service.WorkflowResultService;
import edu.indiana.dlib.amppd.web.GalaxyJobState;
import edu.indiana.dlib.amppd.web.ItemSearchResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;
import lombok.extern.slf4j.Slf4j;

//@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class WorkflowResultController {
	
	@Autowired
	private PrimaryfileRepository primaryfileRepository;

	@Autowired
	private WorkflowResultRepository workflowResultRepository;

	@Autowired
	private MediaService mediaService;

	@Autowired
	private WorkflowResultService workflowResultService;
	
	@Autowired
	private PermissionService permissionService;
	
	
	/**
	 * Get a list of all workflow results satisfying the given query.
	 * @param query the search query for workflow results
	 * @return the WorkflowResultResponse containing the list of queried workflow results
	 */
	@PostMapping(path = "/workflow-results/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public WorkflowResultResponse getWorkflowResults(@RequestBody WorkflowResultSearchQuery query){
		// AC prefilter on WorkflowResultSearchQuery to restrict unit filters to only accessible ones by current user
		Set<Long> accessibleUnits = permissionService.prefilter(query);
		
		// Note:
		// This should better be a GET request instead of POST, according to REST API standards.
		// However, Axios doesn't support GET with body, while sending the request with the query as a param  
		// could result in a very long URL, which might exceeds the URL length limit.
		log.info("Retrieving WorkflowResults for query ...");		
		WorkflowResultResponse response = workflowResultService.getWorkflowResults(query);
		
		// AC postfilter on WorkflowResultResponse to restrict WorkflowResultFilterValues.units to only accessible ones by current user
		permissionService.postfilter(response, accessibleUnits);		
		return response;
	}

	/**
	 * Get a list of primaryfiles wrapped in parent items for a partial workflow that takes an input primaryfile of the given mediaType, with name matching the given keyword, 
	 * and with completed intermediate result outputs for each data type in the given outputTypes list.
	 * @param mediaType the given mediaType
	 * @param keyword the given keyword
	 * @param outputTypes the given outputTypes
	 * @return itemSearchResponse a list of items containing primaryfiles satisfying above criteria
	 */
	@GetMapping(path = "/workflow-results/intermediate/primaryfiles", produces = MediaType.APPLICATION_JSON_VALUE)
	public ItemSearchResponse getPrimaryfilesForPartialWorkflow(
			@RequestParam(required = false) String mediaType, 
			@RequestParam(required = false) String keyword, 
			@RequestParam List<String> outputTypes) {
		// get accessible units for Read WorkflowResult (if none, access deny exception will be thrown)
		// otherwise if accessibleUnits is null, i.e. user is admin, then no AC prefilter is needed; 
		// otherwise, all queries on WorkflowResult below are limited within the accessible units
		Set<Long> acUnitIds = permissionService.getAccessibleUnits(ActionType.Read, TargetType.WorkflowResult);
		ItemSearchResponse response = new ItemSearchResponse();		
				
		// ensure that keyword has a value for below query
		// Note: when keyword is empty, any name is matched 
		if (keyword == null) {
			keyword = "";
		}

		// if not all given output types exist in the workflow result table, return empty list
		int count = acUnitIds == null ?
				workflowResultRepository.countDistinctOutputTypes(outputTypes) :
				workflowResultRepository.countDistinctOutputTypesAC(outputTypes, acUnitIds);
		if (count < outputTypes.size()) {
			log.info("Retrieved none legitimate primaryfile with COMPLETE outputs for all " + outputTypes.size() + " output types.");
			return response;
		}
		
		// otherwise retrieve the primaryfiles with outputs for all of the given types
		List<PrimaryfileIdInfo> pidis = acUnitIds == null ?
				workflowResultRepository.findPrimaryfileIdsByOutputType(keyword, outputTypes) :
				workflowResultRepository.findPrimaryfileIdsByOutputTypeAC(keyword, outputTypes, acUnitIds);
		List<ItemInfo> itemis = response.getItems();	
		ItemInfo itemi = null;	// current item 
		int countp = 0;	// count of matching primaryfiles
		
//		// if primaryfile is not used as one input or the required input media type is AV, return above primaryfiles list
//		if (mediaService.isMediaTypeAV(mediaType)) {
//			log.info("Retrieved " + pfs.size() + " primaryfiles with keyword " + keyword + " and COMPLETE outputs for all " + outputTypes.size() + " output types.");
//		}		
		
		// only include primaryfiles with matching media type
		for (PrimaryfileIdInfo pidi : pidis) {	
			Long pid = pidi.getPrimaryfileId();
			Primaryfile primaryfile = primaryfileRepository.findById(pid).orElseThrow(() -> new StorageException("Primaryfile <" + pid + "> does not exist!"));
			
			// if current primaryfile belongs to another item than the current item, start a new item as the current item
			Item item = primaryfile.getItem();
			Collection collection = item.getCollection();
			if (itemi == null || itemi.getItemId().longValue() != item.getId().longValue()) {
				itemi = new ItemInfo(collection.getId(), collection.getName(), item.getId(), item.getName(), item.getExternalSource(), item.getExternalId(), new ArrayList<PrimaryfileInfo>());
			}
			
			// if current primaryfile MIME type matches the required media type, add it to the current item 
			List<PrimaryfileInfo> pfileis = itemi.getPrimaryfiles();
			String mimeType = primaryfile.getMimeType();
			if (mediaService.isMediaTypeMatched(mimeType, mediaType)) {
				PrimaryfileInfo pfilei = new PrimaryfileInfo(pid, primaryfile.getName(), mimeType, primaryfile.getOriginalFilename());
				pfileis.add(pfilei);
				countp++;

				// add the current item to the item list when its first primaryfile is added 
				if (pfileis.size() == 1) {
					itemis.add(itemi);				
				}	
			}
		}

		log.info("Retrieved " + countp + " primaryfiles with mediaType " + mediaType + ", keyword " + keyword + ", and COMPLETE outputs for all " + outputTypes.size() + " output types.");
		return response;
	}
		
	/**
	 * Get a list of intermediate workflow results in COMPLETE status associated with the given primaryfile for each data type in the given outputTypes list.
	 * @param primaryfileId ID of the given primaryfile
	 * @param outputTypes the given outputTypes list
	 * @return list of workflow results grouped by output types for the primaryfile
	 */
	@GetMapping(path = "/workflow-results/intermediate/outputs", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<List<WorkflowResult>> getCompleteWorkflowResultsForPrimaryfileOutputTypes(@RequestParam Long primaryfileId, @RequestParam List<String> outputTypes) {
		List<List<WorkflowResult>> resultss = new ArrayList<List<WorkflowResult>>();
		for (String outputType : outputTypes) {
			List<WorkflowResult> results = workflowResultRepository.findByPrimaryfileIdAndOutputTypeAndStatus(primaryfileId, outputType, GalaxyJobState.COMPLETE);			
			resultss.add(results);
		}
		log.info("Retrieved " + resultss.size() + " groups of intermediate workflow results with COMPLETE outputs for " + " primaryfile " + primaryfileId + " and " + outputTypes.size() + " output types.");
		return resultss;
	}
		
	/* TODO
	 * More request params can be added to allow various scope of partial refresh. 
	 * For ex, the scope of records to be refreshed can be defined by the following criteria:
	 * - data entity ID or name pattern for primaryfile, bundle, item, collection, unit 
	 * - workflow (ID, name pattern)
	 * - date created/updated/refreshed
	 * Among above, specifying primaryfile/bundle ID is particularly useful if AMP jobs creation was successful 
	 * but results failed to be added upon workflow submission, then we can manually run the refresh on the 
	 * parimaryfile/bundle to add the results.
	 */	
	/**
	 * Refresh the whole WorkflowResults table iteratively by retrieving and processing workflow invocations per primaryfile,
	 * unless the lumpsum mode is specified and true, in which case, retrieve and process all workflow invocations at once.
	 * It's recommended to turn lumpsum off if request to Galaxy tends to timeout due to large amount of records.
	 * The WorkflowResult table is typically refreshed for the following cases:
	 * - initial population of the table;
	 * - new fields are added;
	 * - non ID fields (for ex, names) have value changes across many rows;
	 * - the table is compromised (for ex, due to system exceptions, accidental manual operations).
	 * @param lumpsum whether to refresh the table in the lumpsum mode
	 * @return the number of WorkflowResult refreshed
	 */	
	@PostMapping("/workflow-results/refresh")
	public int refreshWorkflowResults(@RequestParam(required = false) Boolean lumpsum) {
		if (lumpsum != null && lumpsum) {
			log.info("Refreshing Workflow Results in a lump sum manner ... ");
			return workflowResultService.refreshWorkflowResultsLumpsum().size();
		}
		else {
			log.info("Refreshing Workflow Results iteratively per primaryfile ... ");
			return workflowResultService.refreshWorkflowResultsIterative().size();
		}
	}

	/**
	 * Fix workflow results with obsolete output types with correct data types and update the corresponding datasets in Galaxy.
	 * Note that this is a one-time operation to back-fill existing data. We don't need this endpoint on-going.
	 * @return the list of WorkflowResults updated
	 */
	@PostMapping("/workflow-results/output-type")
	public int fixWorkflowResultsOutputType() {
		log.info("Fixing workflow results with obsolete output types ...");
		return workflowResultService.fixWorkflowResultsOutputType().size();
	}

	/**
	 * This method is deprecated, please use setRelevantWorkflowResults instead.
	 * Hide all irrelevant workflow results by setting its corresponding output dataset in Galaxy to invisible,
	 * and remove the row from the WorkflowResult table. This process only needs to be done once manually (preferably 
	 * when refresh table job is not running) when somehow irrelevant outputs failed to be set as invisible in Galaxy.
	 * @return the number of WorkflowResults updated
	 */
	@Deprecated
	@PostMapping("/workflow-results/hide")
	public int hideIrrelevantWorkflowResults() {
		log.info("Hiding irrelevant workflow results ...");
		return workflowResultService.hideIrrelevantWorkflowResults().size();
	}

	/**
	 * Set the WorkflowResults matching the given list of workflow-step-output maps as relevant/irrelevant, 
	 * and update their corresponding output datasets in Galaxy as visible/invisible accordingly.
	 * Note that if a wild-card is used in a field of a search criteria map, then that criteria matches all values of that field.
	 * @param workflowStepOutputs the list of workflowId-workflowStep-outputName maps identifying the results to be set
	 * @param relevant indicator on whether or not to set WorkflowResults as relevant
	 * @return the number of WorkflowResults updated
	 */
	@PostMapping(path = "/workflow-results/relevant", consumes = MediaType.APPLICATION_JSON_VALUE)
	public int setRelevantWorkflowResults(@RequestBody List<Map<String, String>> workflowStepOutputs, @RequestParam Boolean relevant) {
		log.info("Setting workflow results relevant to " + relevant + " with given criteria ...");
		return workflowResultService.setRelevantWorkflowResults(workflowStepOutputs, relevant).size();
	}
	
	/**
	 * Update the specified WorkflowResult according to the specified output label and final status;
	 * if outputLabel or isFinal is not provided, then no update on the corresponding field.
	 * @param workflowResultId id of the specified WorkflowResult
	 * @param outputLabel the specified output label
	 * @param isFinal the specified final status
	 * @return WorkflowResult updated
	 */
	@PatchMapping(path = "/workflow-results/{workflowResultId}")
	public WorkflowResult updateWorkflowResult(@PathVariable Long workflowResultId, @RequestParam(required = false) String outputLabel, @RequestParam(required = false) Boolean isFinal){
		log.info("Updating workflow result "  + workflowResultId + ": outputLabel + " + outputLabel + "  isfinal = " + isFinal);
		return workflowResultService.updateWorkflowResult(workflowResultId, outputLabel, isFinal);
	}

	/**
	 * Delete the specified WorkflowResult from AMP table and Galaxy history.
	 * @param workflowResultId id of the specified WorkflowResult
	 * @return WorkflowResult deleted
	 */
	@DeleteMapping(path = "/workflow-results/{workflowResultId}")
	public WorkflowResult deleteWorkflowResult(@PathVariable Long workflowResultId) {
		log.info("Deleting workflow result "  + workflowResultId + workflowResultId);
		return workflowResultService.deleteWorkflowResult(workflowResultId);		
	}
	
	/**
	 * Set and export workflow result csv file as part of response
	 * @param response HttpServletResponse
	 * @param query WorkflowResultSearchQuery
	 * @return the number of WorkflowResults updated
	 */	
	@PostMapping(path = "/workflow-results/export", consumes = MediaType.APPLICATION_JSON_VALUE)
	public int exportToCSV(HttpServletResponse response, @RequestBody WorkflowResultSearchQuery query) throws IOException {
        response.setContentType("text/csv");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateTime = dateFormatter.format(new Date());
         
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=AmpDashboardExport_" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);

		log.info("Exporting CSV " + headerValue);		
		return workflowResultService.exportWorkflowResults(response, query).size();
    }
	
}
