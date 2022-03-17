package edu.indiana.dlib.amppd.config;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.service.WorkflowResultService;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WorkflowResultsScheduler {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	@Autowired
	private WorkflowResultService workflowResultService;

	// Runs every 10 minutes on the 10th minute from 6:00am through 23:59pm
	@Scheduled(cron = "${amppd.refreshResultsStatusCron}")
	public void refreshStatus() {
		log.info("Starting refresh status at " + sdf.format(new Date()));
		workflowResultService.refreshIncompleteWorkflowResults();
		log.info("Finished running refresh status at " + sdf.format(new Date()));
	}

	// Runs every night at 1 am
	@Scheduled(cron = "${amppd.refreshResultsTableCron}")
	public void refreshAllResults() {
		log.info("Starting refreshWorkflowResultsIterative at " + sdf.format(new Date()));
		workflowResultService.refreshWorkflowResultsIterative();
		log.info("Finished refreshWorkflowResultsIterative at " + sdf.format(new Date()));
	}
}