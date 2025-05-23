package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationDetails;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;
import com.jayway.jsonpath.JsonPath;

import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.security.JwtTokenUtil;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.util.TestHelper;

// TODO remove ignore once we have Galaxy Bootstrap working on Bamboo
@Ignore
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class JobControllerTests {

	public static final Long BUNDLE_ID = 2l;
	public static final String TOOL_ID = "remove_trailing_silence";
	
	@MockBean
    private BundleRepository bundleRepository;
		
	@Autowired
	private JobService jobService;   
			
	@Autowired
	private TestHelper testHelper;   

    @Autowired
    private MockMvc mvc;
    
	private Primaryfile primaryfile;
	private Workflow workflow;	
	private Invocation invocation;
	String token = "";

	/* Notes:
	 * The below setup and cleanup methods shall really be at class level instead of method level; however, JUnit requires class level methods to be static, 
	 * which won't work here, since these methods access Spring beans and member fields. As a result, cleanupHistories will not be done for tests in this class,
	 * which is OK as it will be done by the GalaxyDataServiceTests. Another reason we don't want to clean up histories after each test is that we want to reuse 
	 * the AMP job created in setup across job related tests; this makes Galaxy behave more efficiently. Otherwise, some fields in the outputs may not be
	 * populated in time, causing assertions to fail randomly.
	 */
		
	@Before
	public void setup() {
    	// prepare the primaryfile, workflow, and the AMP job for testing
    	primaryfile = testHelper.ensureTestAudio();
    	workflow = testHelper.ensureTestWorkflow();    
    	invocation = testHelper.ensureTestJob(true);  
    	token = testHelper.getToken();
	}
		
	@After
	public void cleanupHistories() {
//		testHelper.cleanupHistories();
	}
		    	    
    @Test
    public void shouldCreateJob() throws Exception {    	              
    	mvc.perform(post("/jobs").header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token).param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()).param("parameters", "{}"))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$.historyId").isNotEmpty()).andExpect(
    							jsonPath("$.outputIds").isNotEmpty());    			
    }
    
    @Test
    public void shouldcreateJobs() throws Exception {    	              
    	// create a dummy bundle 
    	Bundle bundle = new Bundle();
    	bundle.setId(BUNDLE_ID);
    	bundle.setPrimaryfiles(new HashSet<Primaryfile>());
    	Mockito.when(bundleRepository.findById(BUNDLE_ID)).thenReturn(Optional.of(bundle));     	     	

    	// add the valid primaryfile to the bundle
    	bundle.getPrimaryfiles().add(primaryfile);
    	
    	// add some invalid primaryfile to the bundle
    	Primaryfile pf = new Primaryfile();
    	pf.setId(0l);;
    	bundle.getPrimaryfiles().add(pf);
    	
    	// use the dummy bundle we set up for this test
    	mvc.perform(post("/jobs/bundle").header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token).param("workflowId", workflow.getId()).param("bundleId", bundle.getId().toString()).param("parameters", "{}"))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$").isNotEmpty());
    	// TODO fix verification of primaryfile ID existence
//    					jsonPath("$", Matchers.hasKey(primaryfile.getId())));
//    					jsonPath("$[0].historyId").isNotEmpty()).andExpect(
//    							jsonPath("$[0].outputIds").isNotEmpty()).andExpect(
//    	    							jsonPath("$[1]").doesNotExist());    			
    }
    
    @Test
    public void shouldListJobsDetails() throws Exception {    	               	
    	mvc.perform(get("/jobs/details").header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$[0].inputs").isNotEmpty()).andExpect(
    							jsonPath("$[0].steps").isNotEmpty()).andExpect(
    									jsonPath("$[0].steps[0].outputs").isNotEmpty());    			
    }
    
    @Test
    public void shouldListJobs() throws Exception {    	               	
    	mvc.perform(get("/jobs").header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token).param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$[0].historyId").value(primaryfile.getHistoryId())).andExpect(
    							jsonPath("$[0].id").isNotEmpty());    			
    }
    
    @Test
    public void shouldShowJob() throws Exception {    	              	
    	// request to list the jobs and retrieve invocationId from the response
    	MvcResult result = mvc.perform(get("/jobs").header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token).param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()))
    			.andExpect(status().isOk()).andReturn();    	
    	String invocationId = JsonPath.read(result.getResponse().getContentAsString(), "$[0].id");
    	
    	// request to show the job with the retrieved invocationId
    	mvc.perform(get("/jobs/{invocationId}", invocationId).header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token).param("workflowId", workflow.getId()))
    		.andExpect(status().isOk()).andExpect(
    			jsonPath("$.id").value(invocationId)).andExpect(
    				jsonPath("$.inputs").isNotEmpty()).andExpect(
    	    			jsonPath("$.steps[2].id").isNotEmpty()).andExpect(
   	    	    			jsonPath("$.steps[2].updateTime").isNotEmpty()).andExpect(
   	   	    	    			jsonPath("$.steps[2].jobId").isNotEmpty()).andExpect(
 	    	   	    	    		jsonPath("$.steps[2].orderIndex").value(2)).andExpect(
 	    	    	   	    	    	jsonPath("$.steps[2].state").value("scheduled")).andExpect(
 	    	    	   	    	    		jsonPath("$.steps[3]").doesNotExist());
    }
    
    @Test
    public void shouldShowJobStep() throws Exception {    	               	
    	// request to list the jobs and retrieve the invocationId from the response
    	MvcResult result1 = mvc.perform(get("/jobs").header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token).param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()))
    			.andExpect(status().isOk()).andReturn();    	
    	String invocationId = JsonPath.read(result1.getResponse().getContentAsString(), "$[0].id");
    	    	
    	// request to show the job with the retrieved invocationId
    	MvcResult result2 = mvc.perform(get("/jobs/{invocationId}", invocationId).header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token).param("workflowId", workflow.getId()))
    		.andExpect(status().isOk()).andReturn();   
    	String stepId = JsonPath.read(result2.getResponse().getContentAsString(), "$.steps[2].id");

    	// request to show the step with the retrieved invocationId and stepId
    	mvc.perform(get("/jobs/{invocationId}/steps/{stepId}", invocationId, stepId).header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token).param("workflowId", workflow.getId()))
    		.andExpect(status().isOk()).andExpect(
    			jsonPath("$.id").value(stepId)).andExpect(
    				jsonPath("$.jobs").isNotEmpty()).andExpect(
    	    	    	jsonPath("$.jobs[0].id").isNotEmpty()).andExpect(
    	    	    		jsonPath("$.jobs[0].toolId").value(TOOL_ID)).andExpect(
    	    	    			jsonPath("$.jobs[0].updated").isNotEmpty()).andExpect(
    	        	    	    		jsonPath("$.jobs[0].state").isNotEmpty()).andExpect(
    	    		jsonPath("$.outputs").isNotEmpty());
    }
       
    @Test
    public void shouldShowJobStepOutput() throws Exception {    	               	
    	String stepId = null;
    	String datasetId = null;
    	
    	if (invocation instanceof WorkflowOutputs) {
        	// retrieve the stepId/outputId using the IDs contained in the workflow outputs after running the AMP job
    		WorkflowOutputs woutputs = (WorkflowOutputs)invocation;
    		stepId = woutputs.getSteps().get(2).getId();
    		datasetId = woutputs.getOutputIds().get(1);
    	}
    	else {
        	// retrieve the stepId/outputId using the IDs contained in the invocation details returned by querying the AMP job
    		InvocationDetails idetails = (InvocationDetails)jobService.getWorkflowsClient().showInvocation(workflow.getId(), invocation.getId(), true);
    		stepId = idetails.getSteps().get(2).getId();
    		datasetId = idetails.getSteps().get(2).getOutputs().get(TestHelper.TEST_OUTPUT).getId();
    	}

    	// request to show the step output with the returned invocationId, stepId, and datasetId
    	mvc.perform(get("/jobs/{invocationId}/steps/{stepId}/outputs/{datasetId}", invocation.getId(), stepId, datasetId).header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token).param("workflowId", workflow.getId()))
    		.andExpect(status().isOk()).andExpect(
    			jsonPath("$.id").value(datasetId)).andExpect(
    	    			jsonPath("$.historyId").value(invocation.getHistoryId())).andExpect(
    	    	    			jsonPath("$.fileName").isNotEmpty());
    }
       
}
