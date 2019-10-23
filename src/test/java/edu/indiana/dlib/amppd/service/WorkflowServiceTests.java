package edu.indiana.dlib.amppd.service;

import org.junit.Assert;
import org.junit.Before;
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
	
	@Before
	public void setup() {
    	// prepare the workflow for testing
    	workflow = testHelper.ensureTestWorkflow();  
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


}
