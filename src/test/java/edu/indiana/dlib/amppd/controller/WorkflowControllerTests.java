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

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.service.WorkflowService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class WorkflowControllerTests {
		
	@Autowired
	private WorkflowService workflowService;   
	
    @Autowired
    private MockMvc mvc;

	@Before
	public void setup() {
		// TODO We need to make sure there're some existing workflows in Galaxy for testing;
		// this can be done via factory to import workflow json files, or populate workflows with Galaxy bootstrap.
		
		// TODO alternatively we could use mock workflowsClient, in which case we won't require any existing data in Galaxy
 	}
	
    @Test
    public void shouldListWorkflows() throws Exception {    	
    	mvc.perform(get("/workflows")).andExpect(status().isOk()).andExpect(
    			// TODO need to import org.hamcrest.Matchers.hasSize with added dependency hamcrest-all, 
    			//jsonPath("$", hasSize(1))).andExpect(	
    			jsonPath("$[0]").exists());
    }


    @Test
    public void shouldShowDetailsOnValidWorkflow() throws Exception {    	
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these
    	Workflow workflow = workflowService.getWorkflowsClient().getWorkflows().get(0); 

    	mvc.perform(get("/workflows/{workflowId}", workflow.getId())).andExpect(status().isOk()).andExpect(
    			jsonPath("$.id").value(workflow.getId()));
    }

    @Test(expected = GalaxyWorkflowException.class)
    public void shouldErrorOnNonExistingWorkflow() throws Exception {    	
    	mvc.perform(get("/workflows/{workflowId}", "0"));
    }

}
