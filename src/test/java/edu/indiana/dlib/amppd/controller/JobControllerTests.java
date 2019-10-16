package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
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
import org.springframework.transaction.annotation.Transactional;

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;
import com.jayway.jsonpath.JsonPath;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.util.TestHelper;

// TODO remove ignore once sample media file is added to repository
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class JobControllerTests {

	public static final Long BUNDLE_ID = 2l;
	
	@MockBean
    private BundleRepository bundleRepository;

	@Autowired
    private PrimaryfileRepository primaryfileRepository;
		
	@Autowired
	private JobService jobService;   
			
	@Autowired
	private TestHelper testHelper;   

    @Autowired
    private MockMvc mvc;
    
	private Primaryfile primaryfile;
	private Workflow workflow;	

	@Before
	public void setup() {
    	// prepare the primaryfile and workflow for testing
    	primaryfile = testHelper.ensureTestAudio();
    	workflow = testHelper.ensureTestWorkflow();    
	}
		
	@After
	public void cleanupHistories() {
		testHelper.cleanupHistories();
	}
		    	    
    @Test
    public void shouldCreateJob() throws Exception {    	              
    	mvc.perform(post("/jobs").param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()).param("parameters", "{}"))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$.historyId").isNotEmpty()).andExpect(
    							jsonPath("$.outputIds").isNotEmpty());    			
    }
    
    @Test
    @Transactional
    public void shouldCreateJobBundle() throws Exception {    	              
    	// add some invalid primaryfile to the valid primaryfile's item
    	Primaryfile pf = new Primaryfile();
    	pf.setId(0l);;
    	primaryfile.getItem().getPrimaryfiles().add(pf); // this requires the method to be transactional otherwise Hibernate will throw LazyInitializationException
    	
    	// create a dummy bundle containing both the above item and some invalid item
    	Bundle bundle = new Bundle();
    	bundle.setId(BUNDLE_ID);
    	Mockito.when(bundleRepository.findById(BUNDLE_ID)).thenReturn(Optional.of(bundle));     	     	
    	Set<Item> items = new HashSet<Item>();
    	items.add(primaryfile.getItem());
    	items.add(new Item());
    	bundle.setItems(items); 	

    	// use the dummy bundle we set up for this test
    	mvc.perform(post("/jobs/bundle").param("workflowId", workflow.getId()).param("bundleId", bundle.getId().toString()).param("parameters", "{}"))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$[0].historyId").isNotEmpty()).andExpect(
    							jsonPath("$[0].outputIds").isNotEmpty()).andExpect(
    	    							jsonPath("$[1]").doesNotExist());    			
    }
    
    @Test
    public void shouldListJobs() throws Exception {    	               	
    	jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());    	
    	Primaryfile savedPrimaryfile = primaryfileRepository.findById(primaryfile.getId()).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfile.getId() + "> does not exist!"));

    	mvc.perform(get("/jobs").param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$[0].historyId").value(savedPrimaryfile.getHistoryId())).andExpect(
    							jsonPath("$[0].id").isNotEmpty()).andExpect(
    	    							jsonPath("$[1]").doesNotExist());    			
    }
    
    @Test
    public void shouldShowJob() throws Exception {    	              	
    	jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());    	

    	// request to list the jobs and retrieve invocationId from the response
    	MvcResult result = mvc.perform(get("/jobs").param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()))
    			.andExpect(status().isOk()).andReturn();    	
    	String invocationId = JsonPath.read(result.getResponse().getContentAsString(), "$[0].id");
    	
    	// request to show the job with the retrieved invocationId
    	mvc.perform(get("/jobs/{invocationId}", invocationId).param("workflowId", workflow.getId()))
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
    	jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());    	

    	// request to list the jobs and retrieve the invocationId from the response
    	MvcResult result1 = mvc.perform(get("/jobs").param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()))
    			.andExpect(status().isOk()).andReturn();    	
    	String invocationId = JsonPath.read(result1.getResponse().getContentAsString(), "$[0].id");
    	    	
    	// request to show the job with the retrieved invocationId
    	MvcResult result2 = mvc.perform(get("/jobs/{invocationId}", invocationId).param("workflowId", workflow.getId()))
    		.andExpect(status().isOk()).andReturn();   
    	String stepId = JsonPath.read(result2.getResponse().getContentAsString(), "$.steps[2].id");

    	// request to show the step with the retrieved invocationId and stepId
    	mvc.perform(get("/jobs/{invocationId}/steps/{stepId}", invocationId, stepId).param("workflowId", workflow.getId()))
    		.andExpect(status().isOk()).andExpect(
    			jsonPath("$.id").value(stepId)).andExpect(
    				jsonPath("$.jobs").isNotEmpty()).andExpect(
    	    	    	jsonPath("$.jobs[0].id").isNotEmpty()).andExpect(
    	    	    		jsonPath("$.jobs[0].toolId").value("ES-1")).andExpect(
    	    	    			jsonPath("$.jobs[0].updated").isNotEmpty()).andExpect(
    	        	    	    		jsonPath("$.jobs[0].state").isNotEmpty()).andExpect(
    	    		jsonPath("$.outputs").isNotEmpty());
    }
       
    @Test
    public void shouldShowJobStepOutput() throws Exception {    	               	
    	WorkflowOutputs outputs = jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());    	

    	// request to show the step output with the returned invocationId, stepId, and datasetId
    	mvc.perform(get("/jobs/{invocationId}/steps/{stepId}", workflow.getId(), outputs.getId(), outputs.getSteps().get(2).getId(), outputs.getOutputIds().get(1)))
    		.andExpect(status().isOk()).andExpect(
    			jsonPath("$.id").value(outputs.getOutputIds().get(1))).andExpect(
    	    			jsonPath("$.historyId").value(outputs.getHistoryId())).andExpect(
    	    	    			jsonPath("$.fileName").isNotEmpty());
    }
       

}
