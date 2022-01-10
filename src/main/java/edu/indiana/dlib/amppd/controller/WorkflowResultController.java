package edu.indiana.dlib.amppd.controller;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.WorkflowResultService;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;
import lombok.extern.slf4j.Slf4j;

//@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class WorkflowResultController {
	
	@Autowired
	private WorkflowResultService workflowResultService;
	
	/**
	 * Get a list of all workflow results satisfying the given query.
	 * @param query the search query for workflow results
	 * @return the WorkflowResultResponse containing the list of queried workflow results
	 */
	@PostMapping(path = "/workflow-results", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public WorkflowResultResponse getWorkflowResults(@RequestBody WorkflowResultSearchQuery query){
		log.info("Retrieving WorkflowResults for query ...");
		return workflowResultService.getWorkflowResults(query);
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
	 * Note that if a wild card is used in a field of a search criteria map, then that criteria matches all values of that field.
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
	 * Sets the specified WorkflowResult according to the specified final status
	 * @param WorkflowResultId id of the specified WorkflowResult
	 * @param isFinal the specified final status
	 * @return true if request is successful; false otherwise
	 */
	@PostMapping(path = "/workflow-results/{id}")
	public boolean setFinalWorkflowResult(@PathVariable Long id, @RequestParam Boolean isFinal){
		log.info("Setting workflow result "  + id + " final to " + isFinal);
		return workflowResultService.setFinalWorkflowResult(id, isFinal) != null;
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
