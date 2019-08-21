package edu.indiana.dlib.amppd.service;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowServiceTests {

	@Autowired
	private WorkflowService workflowService;   
		
    @Test
    public void shouldBuildWorkflowInputs() {
    	WorkflowInputs winputs = workflowService.buildWorkflowInputs(workflowId, datasetId, parameters);
    	Assert.assertNotNull(dataset);
    }
    
    @Test(expected = GalaxyWorkflowException.class)
    public void shouldThrowExceptionBuildingInputsForNonExistingWorkflow() {
    	workflowService.buildWorkflowInputs("foobar", "", new HashMap<String, Map<String, String>>());
    }


}
