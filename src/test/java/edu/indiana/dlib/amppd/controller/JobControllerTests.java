package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.jayway.jsonpath.JsonPath;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.util.TestHelper;

// TODO remove ignore once sample media file is added to repository
@Ignore
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class JobControllerTests {

	public static final String TEST_DIRECTORY_NAME = "test";
	public static final String PRIMARYFILE_NAME = "primaryfile.mp4";
	public static final Long PRIMARYFILE_ID = 1l;
	public static final Long BUNDLE_ID = 2l;
	
	@MockBean
    private BundleRepository bundleRepository;

	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
    private FileStorageService fileStorageService;
		
	@Autowired
	private JobService jobService;   
			
	@Autowired
	private TestHelper testHelper;   

    @Autowired
    private MockMvc mvc;
    
    private Primaryfile primaryfile;
    private Bundle bundle;

    /**
     * Set up a dummy primaryfile in Amppd for testing workflow.
     */
    private void setUpPrimaryFile() {
    	primaryfile = new Primaryfile();
    	primaryfile.setId(PRIMARYFILE_ID);
    	primaryfile.setPathname(TEST_DIRECTORY_NAME + "/" + PRIMARYFILE_NAME);
    	
    	Path unitpath = fileStorageService.resolve(TEST_DIRECTORY_NAME);
    	Path path = fileStorageService.resolve(primaryfile.getPathname());
    	
    	try {
    		Files.createDirectories(unitpath);
    		Files.createFile(path);
    	}        
    	catch (FileAlreadyExistsException e) {
        	// if the file already exists do nothing
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Can't create test file for GalaxyDataServiceTests.", e);
    	} 	
    	
//    	Mockito.when(primaryfileRepository.findById(PRIMARYFILE_ID)).thenReturn(Optional.of(primaryfile));     	    	
    }

    /**
     * Set up a dummy bundle in Amppd for testing Amppd job bundle creation.
     */
    private void setUpBundle() {    	
    	Item item = new Item();
    	Set<Primaryfile> primaryfiles = new HashSet<Primaryfile>();
    	primaryfiles.add(primaryfile);
    	item.setPrimaryfiles(primaryfiles);

    	// add some invalid primaryfile to the item
    	Primaryfile pf = new Primaryfile();
    	pf.setId(0l);;
    	primaryfiles.add(pf);
    	
    	bundle = new Bundle();
    	Set<Item> items = new HashSet<Item>();
    	items.add(item);
    	bundle.setItems(items); 	

    	// add some invalid item to the bundle
    	items.add(new Item());

    	bundle.setId(BUNDLE_ID);
    	Mockito.when(bundleRepository.findById(BUNDLE_ID)).thenReturn(Optional.of(bundle));     	    	
    }
        
	@Before
	public void setup() {
		// make sure there're some existing primaryfile uploaded in Amppd for testing
		setUpPrimaryFile();
		setUpBundle();
		
		// TODO We need to make sure there're some existing workflows in Galaxy for testing;
		// this can be done via factory to import workflow json files, or populate workflows with Galaxy bootstrap.
		
		// TODO alternatively we could use mock workflowsClient, in which case we won't require any existing data in Galaxy
 	}
	
    @Test
    public void shouldCreateJob() throws Exception {    	              
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these
    	Workflow workflow = jobService.getWorkflowsClient().getWorkflows().get(0); 

    	mvc.perform(post("/jobs").param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()).param("parameters", "{}"))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$.historyId").isNotEmpty()).andExpect(
    							jsonPath("$.outputIds").isNotEmpty());    			
    }
    
    @Test
    public void shouldCreateJobBundle() throws Exception {    	              
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these
    	Workflow workflow = jobService.getWorkflowsClient().getWorkflows().get(0); 

    	mvc.perform(post("/jobs/bundle").param("workflowId", workflow.getId()).param("bundleId", bundle.getId().toString()).param("parameters", "{}"))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$[0].historyId").isNotEmpty()).andExpect(
    							jsonPath("$[0].outputIds").isNotEmpty()).andExpect(
    	    							jsonPath("$[1]").doesNotExist());    			
    }
    
    @Test
    public void shouldListJobs() throws Exception {    	              
    	// prepare the primaryfile and workflow for testing, then run the AMP job once on them
    	Primaryfile primaryfile = testHelper.ensureTestAudio();
    	Workflow workflow = testHelper.ensureTestWorkflow();    	
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
    	// prepare the primaryfile and workflow for testing, then run the AMP job once on them
    	Primaryfile primaryfile = testHelper.ensureTestAudio();
    	Workflow workflow = testHelper.ensureTestWorkflow();    	
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
    	// prepare the primaryfile and workflow for testing, then run the AMP job once on them
    	Primaryfile primaryfile = testHelper.ensureTestAudio();
    	Workflow workflow = testHelper.ensureTestWorkflow();    	
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
    	        	    	    		jsonPath("$.jobs[0].state").value("ok")).andExpect(
    	    		jsonPath("$.outputs").isNotEmpty());
    }
       

}
