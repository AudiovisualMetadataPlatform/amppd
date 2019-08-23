package edu.indiana.dlib.amppd.service;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.InputSourceType;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.WorkflowInput;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowServiceTests {

	@Autowired
	private GalaxyDataService galaxyDataService;   
		
	@Autowired
	private WorkflowService workflowService;   
		
	@Before
	public void setup() {
		// TODO We need to make sure there're some existing workflows in Galaxy for testing;
		// this can be done via factory to import workflow json files, or populate workflows with Galaxy bootstrap.
		
		// TODO We need to make sure there're some existing dataset in Galaxy for testing
		
		// TODO alternatively we could use mock workflowsClient, in which case we won't require any existing data in Galaxy
 	}
		
    @Test
    public void shouldBuildWorkflowInputs() {
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these
    	Workflow workflow = workflowService.getWorkflowsClient().getWorkflows().get(0); 

    	// we assume there is at least one dataset existing in Galaxy, and we can use one of these
    	LibraryContent dataset = galaxyDataService.getLibrariesClient().getLibraryContents(galaxyDataService.getSharedLibrary().getId()).get(0);

    	// set up some dummy parameters
    	HashMap<String, Map<String, String>> parameters = new HashMap<String, Map<String, String>>();
    	HashMap<String, String> param1 = new  HashMap<String, String>();
    	param1.put("name1", "value1");
    	parameters.put("step1", param1);
    	
    	WorkflowInputs winputs = workflowService.buildWorkflowInputs(workflow.getId(), dataset.getId(), parameters);
    	
    	Assert.assertEquals(winputs.getWorkflowId(), workflow.getId());
    	Assert.assertEquals(winputs.getInputs().size(), 1);
    	WorkflowInput winput = (WorkflowInput)winputs.getInputs().values().toArray()[0];
    	Assert.assertEquals(winput.getId(), dataset.getId());
    	Assert.assertEquals(winput.getSourceType(), InputSourceType.LDDA);
    	Assert.assertEquals(winputs.getWorkflowId(), workflow.getId());
    	Assert.assertEquals(winputs.getParameters().size(), 1);    	
    }

    @Test(expected = GalaxyWorkflowException.class)
    public void shouldThrowExceptionBuildingInputsForNonExistingWorkflow() {
    	workflowService.buildWorkflowInputs("foobar", "", new HashMap<String, Map<String, String>>());
    }

}
