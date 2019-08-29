package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Assert;
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

import com.github.jmchilton.blend4j.galaxy.beans.GalaxyObject;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.GalaxyDataService;
import edu.indiana.dlib.amppd.service.JobService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class JobControllerTests {

	public static final String TEST_DIRECTORY_NAME = "test";
	public static final String PRIMARYFILE_NAME = "primaryfile.mp4";
	public static final Long PRIMARYFILE_ID = 1l;

	@MockBean
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
    private FileStorageService fileStorageService;
		
	@Autowired
	private JobService jobService;   
		
    @Autowired
    private MockMvc mvc;
    
    private Primaryfile primaryfile;

	@Before
	public void setup() {
		// make sure there're some existing primaryfile uploaded in Amppd for testing
		setUpPrimaryFile();
		
		// TODO We need to make sure there're some existing workflows in Galaxy for testing;
		// this can be done via factory to import workflow json files, or populate workflows with Galaxy bootstrap.
		
		// TODO alternatively we could use mock workflowsClient, in which case we won't require any existing data in Galaxy
 	}

    @Test
    public void shouldReturnWorkflows() throws Exception {    	
    	mvc.perform(get("/workflows")).andExpect(status().isOk()).andExpect(
    			// TODO need to import org.hamcrest.Matchers.hasSize with added dependency hamcrest-all, 
    			//jsonPath("$", hasSize(1))).andExpect(	
    			//jsonPath("$[0].model_class").value("StoredWorkflow"));
    			jsonPath("$[0].name").isNotEmpty());
    }
    

	@Autowired
	private GalaxyDataService galaxyDataService;
    
    @Test
    public void shouldRunWorkflows() throws Exception {    	              
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these
    	Workflow workflow = jobService.getWorkflowsClient().getWorkflows().get(0); 

//    	mvc.perform(post("/workflows/" + workflow.getId() + "/run").param("primaryfileId", primaryfile.getId().toString()).param("parameters", "{}"))
//    			.andExpect(status().isOk()).andExpect(
//    					jsonPath("$.historyId").isNotEmpty()).andExpect(
//    							jsonPath("$.outputIds").isNotEmpty());    	
		
    	// at this point the primaryfile shall have been created and its media file uploaded into Amppd file system
//    	Primaryfile primaryfile = primaryfileRepository.findById(primaryfile.getId()).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfile.getId() + "> does not exist!"));    
    	if (primaryfile.getPathname() == null || primaryfile.getPathname().isEmpty()) {
    		throw new StorageException("Primaryfile " + primaryfile.getId() + " hasn't been uploaded to AMPPD file system");
    	}
    	
    	/* Note: 
    	 * We do a lazy upload from Amppd to Galaxy, i.e. we do it when workflow is invoked in Galaxy, rather than when the file is uploaded to Amppd.
    	 * The pros is that we won't upload to Galaxy unnecessarily if the primaryfile is never going to be processed through workflow;
    	 * the cons is that it might slow down workflow execution when running in batch.
    	 */

    	// upload the primaryfile into Galaxy data library, the returned result is an GalaxyObject containing the ID and URL of the dataset uploaded
    	String pathname = fileStorageService.absolutePathName(primaryfile.getPathname());
    	GalaxyObject go = galaxyDataService.uploadFileToGalaxy(pathname);		
		
    	// invoke the workflow 
    	Map<String, Map<String, String>> params = new HashMap<String, Map<String, String>>();
    	Map<String, String> param = new HashMap<String, String>();
    	param.put("channels", "2");
    	param.put("samplesize", "pcm_s24le");
    	params.put("1", param);
    	WorkflowInputs winputs = jobService.buildWorkflowInputs(workflow.getId(), go.getId(), params);
//    	WorkflowInputs winputs = jobService.buildWorkflowInputs(workflow.getId(), go.getId(), new HashMap<String, Map<String, String>>());
//    	WorkflowInput winput = new WorkflowInput("0d16186aaff7cbfd", InputSourceType.HDA);
//		winputs.setInput("0", winput);		

    	WorkflowOutputs woutputs = jobService.getWorkflowsClient().runWorkflow(winputs);
    	Assert.assertNotNull(woutputs);
    	Assert.assertNotNull(woutputs.getHistoryId());

    }
    
    /**
     * Set up a dummy primaryfile in Amppd for testing workflow.
     */
    private void setUpPrimaryFile() {
    	primaryfile = new Primaryfile();
    	primaryfile.setId(1l);
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
    	
    	Mockito.when(primaryfileRepository.findById(4l)).thenReturn(Optional.of(primaryfile));     	    	
    }

}
