package edu.indiana.dlib.amppd.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.service.WorkflowResultService;

import java.text.SimpleDateFormat;
import java.util.Date;
@Component
public class WorkflowResultsScheduler {
   private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

   @Autowired
   private WorkflowResultService workflowResultService;
   
   // On the 20 minute mark
   // THis will run every 5 seconds for testing @Scheduled(cron = "0/5 * * ? * *")
   @Scheduled(cron = "0 0/20 * ? * *")
   public void refreshStatus() {
      System.out.println("Starting refresh status at " + sdf.format(new Date()));
      workflowResultService.refreshIncompleteResults();
      System.out.println("Finished running refresh status at " + sdf.format(new Date()));
   }
   
   
   // Every day at 1 am
   @Scheduled(cron = "0 0 1 1/1 * *")
   public void refreshAllResults() {
	      System.out.println("Starting refreshWorkflowResultsIterative at " + sdf.format(new Date()));
	      workflowResultService.refreshWorkflowResultsIterative();
	      System.out.println("Finished refreshWorkflowResultsIterative at " + sdf.format(new Date()));
   }
}