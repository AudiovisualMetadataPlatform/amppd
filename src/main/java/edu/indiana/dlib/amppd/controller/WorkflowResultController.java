package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.WorkflowResultService;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class WorkflowResultController {
	
	@Autowired
	private WorkflowResultService workflowResultService;
	
	/**
	 * Get a all workflow results satisfying the given query.
	 * @param query the search query for workflow results
	 * @return the WorkflowResultResponse containing the list of queried workflow results
	 */
	@PostMapping(path = "/workflow-results", consumes = "application/json", produces = "application/json")
	public WorkflowResultResponse getWorkflowResults(@RequestBody WorkflowResultSearchQuery query){
		log.info("Received request inside getWorkflowResults");
		return workflowResultService.getWorkflowResults(query);
	}
	
	/**
	 * Set the given WorkflowRresult isFinal status to the given value.
	 * @param id ID of the given WorkflowRresult
	 * @param isFinal the boolean value of the isFinal status to set
	 * @return true if request is successful; false otherwise
	 */
	@PostMapping(path = "/workflow-results/isfinal/{id}", consumes = "application/json", produces = "application/json")
	public boolean setIsFinal(@PathVariable("id") Long id, @RequestParam("isFinal") boolean isFinal){
		log.info("Setting file to final: " + id);
		return workflowResultService.setResultIsFinal(id, isFinal);
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
	 * Refreshe the whole WorkflowResults table iteratively by retrieving and processing workflow invocations per primaryfile,
	 * unless the lumpsum mode is specified and true, in which case, retrieve and process all workflow invocations at once.
	 * It's recommended to turn lumpsum off if request to Galaxy tends to timeout due to large amount of records.
	 * The WorkflowResult table is typically refreshed for the following cases:
	 * - initial population of the table;
	 * - new fields are added;
	 * - non ID fields (for ex, names) have value changes across many rows;
	 * - the table is compromised (for ex, due to system exceptions, accidental manual operations).
	 * If removeIrrelevant is set to true (false by default), set all irrelevant output datasets to invisible in Galaxy and
	 * remove them from the WorkflowResults table. Note that this removal process only needs to be done by manually refreshing
	 * the table once when somehow irrelevant outputs failed to be set as invisible in Galaxy.
	 * @param lumpsum whether to refresh the table in the lumpsum mode
	 * @param removeIrrelevant whether to set/remove invisible/irrelevant output datasets
	 * @return the list of WorkflowResult refreshed
	 */	
	@PostMapping("/workflow-results/refresh")
	public void refreshWorkflowResults(@RequestParam(required = false) Boolean lumpsum, @RequestParam(required = false) Boolean removeIrrelevant) {
		if (lumpsum != null && lumpsum) {
			log.info("Refreshing Workflow Results in a lump sum manner ... ");
			workflowResultService.refreshWorkflowResultsLumpsum();
		}
		else {
			log.info("Refreshing Workflow Results iteratively per primaryfile ... ");
			workflowResultService.refreshWorkflowResultsIterative();
		}
	}

	/**
	 * Hide all irrelevant workflow results by setting its corresponding output dataset in Galaxy to invisible,
	 * and remove the row from the WorkflowResult table.
	 */
	@PostMapping("/workflow-results/hide")
	public void hideIrrelevantWorkflowResults() {
		log.info("Hiding irrelevant workflow results ...");
		workflowResultService.hideIrrelevantWorkflowResults();
	}
	
}
