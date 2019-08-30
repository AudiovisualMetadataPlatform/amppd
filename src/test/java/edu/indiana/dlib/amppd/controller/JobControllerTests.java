package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

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

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
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
    public void shouldRunWorkflows() throws Exception {    	              
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these
    	Workflow workflow = jobService.getWorkflowsClient().getWorkflows().get(0); 

    	mvc.perform(post("/jobs").param("workflowId", workflow.getId()).param("primaryfileId", primaryfile.getId().toString()).param("parameters", "{}"))
    			.andExpect(status().isOk()).andExpect(
    					jsonPath("$.historyId").isNotEmpty()).andExpect(
    							jsonPath("$.outputIds").isNotEmpty());    			
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
