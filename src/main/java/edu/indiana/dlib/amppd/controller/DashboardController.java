package edu.indiana.dlib.amppd.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.service.DashboardService;
import edu.indiana.dlib.amppd.web.DashboardResponse;
import edu.indiana.dlib.amppd.web.DashboardSearchQuery;
import lombok.extern.slf4j.Slf4j;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class DashboardController {
	
	@Autowired
	private DashboardService dashboardService;
	
	@PostMapping(path = "/dashboard", consumes = "application/json", produces = "application/json")
	public DashboardResponse getDashboardResults(@RequestBody DashboardSearchQuery query){
		log.info("Received request inside getDashboardResults");
		return dashboardService.getDashboardResults(query);
	}
	
	@PostMapping(path = "/dashboard/isfinal/{id}", consumes = "application/json", produces = "application/json")
	public boolean setIsFinal(@PathVariable("id") Long id, @RequestParam("isFinal") boolean isFinal){
		log.info("Setting file to final: " + id);
		return dashboardService.setResultIsFinal(id, isFinal);
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
	 * Refreshes the whole DashboardResults table iteratively by retrieving and processing workflow invocations per primaryfile,
	 * unless the lumpsum mode is specified and true, in which case, retrieve and process all workflow invocations at once.
	 * It's recommended to turn lumpsum off if request to Galaxy tends to timeout due to large amount of records.
	 * The DashboardResults table is typically refreshed for the following cases:
	 * - initial population of the table;
	 * - new fields are added;
	 * - non ID fields (for ex, names) have value changes across many rows;
	 * - the table is compromised (for ex, due to system exceptions, accidental manual operations).
	 * @param lumpsum whether to refresh the table in the lumpsum mode
	 * @return the list of DashboardResults refreshed
	 */	
	@PostMapping("/dashboard/refresh")
	public void refreshDashboardResults(@RequestParam(required = false) Boolean lumpsum) {
		if (lumpsum != null && lumpsum) {
			log.info("Refreshing DashboardResults in a lump sum manner ... ");
			dashboardService.refreshDashboardResultsLumpsum();
		}
		else {
			log.info("Refreshing DashboardResults iteratively per primaryfile ... ");
			dashboardService.refreshDashboardResultsIterative();
		}
	}

}
