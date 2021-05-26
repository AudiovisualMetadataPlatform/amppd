package edu.indiana.dlib.amppd.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

import edu.indiana.dlib.amppd.util.TestHelper;


@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowServiceTests {
		
	@Autowired
	private WorkflowService workflowService;   
	
	@Autowired
	private TestHelper testHelper;   

	private Workflow workflow;	
	private Workflow workflowPublished;	
	
	@Before
	public void setup() {
    	// prepare the workflow for testing
    	workflow = testHelper.ensureTestWorkflow();  
    	workflowPublished = testHelper.ensureTestWorkflowPublished();  
 	}
		
    @Test
    public void shouldIdentifyWorkflowHasTagOrNot() {
    	// workflowPublished should contain the published tag while workflow should not
    	Assert.assertTrue(workflowService.hasWorkflowTag(workflowPublished, "published"));
    	Assert.assertFalse(workflowService.hasWorkflowTag(workflow, "published"));
    }

    @Test
    public void shouldIdentifyWorkflowPublishedOrNot() {
    	// workflowPublished should be identified as published tag while workflow should not
    	Assert.assertTrue(workflowService.isWorkflowPublished(workflowPublished));
    	Assert.assertFalse(workflowService.isWorkflowPublished(workflow));
    }

    @Test
    public void shouldListPublishedWorkflows() {
    	// list both published and unpublished workflows
    	List<Workflow> workflows = workflowService.listWorkflows(true, null, null);
    	
    	// there should be at least 1 published workflows
    	Assert.assertNotNull(workflows);
    	Assert.assertTrue(workflows.size() >= 1);
    	
    	// all listed workflows should be published
    	for (Workflow workflow : workflows) {
    		Assert.assertTrue(workflowService.isWorkflowPublished(workflow));
    	}
    }

    @Test
    public void shouldListUnpublishedWorkflows() {
    	// list both published and unpublished workflows
    	List<Workflow> workflows = workflowService.listWorkflows(false, null, null);
    	
    	// there should be at least 1 unpublished workflows
    	Assert.assertNotNull(workflows);
    	Assert.assertTrue(workflows.size() >= 1);
    	
    	// all listed workflows should be unpublished
    	for (Workflow workflow : workflows) {
    		Assert.assertFalse(workflowService.isWorkflowPublished(workflow));
    	}
    }

    @Test
    public void shouldGetExistingWorkflow() {
    	// retrieve the workflow by name
    	Workflow workflowRetrieved = workflowService.getWorkflow(workflow.getName());
    	
    	// verify the retrieved workflow
    	Assert.assertNotNull(workflowRetrieved.getId());
    	Assert.assertEquals(workflow.getName(), workflowRetrieved.getName());
    }

    @Test
    public void shouldReturnNullOnNonExistingWorkflow() {  
    	// retrieve the workflow by name
    	Workflow workflowRetrieved = workflowService.getWorkflow("foo");
    	
    	// verify the retrieved workflow
    	Assert.assertNull(workflowRetrieved);
    }

    @Ignore
    @Test
    public void shouldGetStoredWorkflowName() {
    	workflowService.clearWorkflowNamesCache();
    	String name = workflowService.getWorkflowName(workflow.getId());
    	Assert.assertEquals(name, workflow.getName());  	
    	Assert.assertEquals(workflowService.workflowNamesCacheSize(), (Integer)1);  	
    }
    
    @Test
    public void shouldGetNonStoredWorkflowIdAsName() {
    	workflowService.clearWorkflowNamesCache();
    	String id = "nonstoredworkflowid";
    	
    	// first call to get name, cache size increased to 1
    	String name = workflowService.getWorkflowName(id);
    	Assert.assertEquals(name, id);
    	Assert.assertEquals(workflowService.workflowNamesCacheSize(), (Integer)1);
    	
    	// second call to get name for the same id, cache is hit and size remains 1
    	workflowService.getWorkflowName(id);
    	Assert.assertEquals(workflowService.workflowNamesCacheSize(), (Integer)1);
    }
    
}
