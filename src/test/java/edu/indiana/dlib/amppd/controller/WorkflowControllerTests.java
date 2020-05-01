package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

import edu.indiana.dlib.amppd.service.WorkflowService;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class WorkflowControllerTests {
		
	@Autowired
	private WorkflowService workflowService;   

	@Autowired
	private TestHelper testHelper;   
	
    @Autowired
    private MockMvc mvc;

	private Workflow workflow;	

	String token = "";
	
	@Before
	public void setup() {
    	// prepare the workflow for testing
    	workflow = testHelper.ensureTestWorkflow();  
    	token = testHelper.getToken();
 	}
	
    @Test
    public void shouldListWorkflows() throws Exception {    	
    	mvc.perform(get("/workflows").header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
    			jsonPath("$[0]").exists());
    }


    @Test
    public void shouldShowWorkflowDetails() throws Exception {    	
    	// there should be at least one workflow existing in Galaxy, and we can use one of these
    	Workflow workflow = workflowService.getWorkflowsClient().getWorkflows().get(0); 

    	mvc.perform(get("/workflows/{workflowId}", workflow.getId()).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
    			jsonPath("$.id").value(workflow.getId()));
    }

}
