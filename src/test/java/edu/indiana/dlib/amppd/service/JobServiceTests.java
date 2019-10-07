package edu.indiana.dlib.amppd.service;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.ExistingHistory;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.InputSourceType;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.WorkflowInput;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Bundle;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class JobServiceTests {

	public static final String TEST_DIRECTORY_NAME = "test";
	public static final String PRIMARYFILE_NAME = "primaryfile.mp4";
	public static final Long PRIMARYFILE_ID = 1l;
	public static final Long BUNDLE_ID = 2l;



	@MockBean
    private BundleRepository bundleRepository;

	@MockBean
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
    private FileStorageService fileStorageService;
	
	@Autowired
	private GalaxyDataService galaxyDataService;   
		
	@Autowired
	private JobService jobService;   
		
	@Autowired
	private TestHelper testHelper;   
		
    private Primaryfile primaryfile;
    private Bundle bundle;
	
    /**
     * Set up a dummy primaryfile in Amppd for testing Amppd job creation.
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
    	
    	Mockito.when(primaryfileRepository.findById(PRIMARYFILE_ID)).thenReturn(Optional.of(primaryfile));     	    	
    	Mockito.when(primaryfileRepository.save(primaryfile)).thenReturn(primaryfile);     	    	
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
		setUpPrimaryFile();
		setUpBundle();
		
		// TODO We need to make sure there're some existing workflows in Galaxy for testing;
		// this can be done via factory to import workflow json files, or populate workflows with Galaxy bootstrap.
		
		// TODO We need to make sure there're some existing dataset in Galaxy for testing
		
		// TODO alternatively we could use mock workflowsClient, in which case we won't require any existing data in Galaxy
 	}
		
    @Test
    public void shouldBuildWorkflowInputsOnValidInputs() {
    	
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these
    	Workflow workflow = jobService.getWorkflowsClient().getWorkflows().get(0); 

    	// we assume there is at least one dataset existing in Galaxy, and we can use one of these
    	LibraryContent dataset = galaxyDataService.getLibrariesClient().getLibraryContents(galaxyDataService.getSharedLibrary().getId()).get(0);

    	// we assume there is at least one history existing in Galaxy, and we can use one of these
    	History history = galaxyDataService.getHistoriesClient().getHistories().get(0);

    	// set up some dummy parameters
    	HashMap<String, Map<String, String>> parameters = new HashMap<String, Map<String, String>>();
    	HashMap<String, String> param1 = new  HashMap<String, String>();
    	param1.put("name1", "value1");
    	parameters.put("step1", param1);
    	
    	WorkflowInputs winputs = jobService.buildWorkflowInputs(workflow.getId(), dataset.getId(), history.getId(), parameters);
    	
    	Assert.assertEquals(winputs.getWorkflowId(), workflow.getId());
    	Assert.assertTrue(((ExistingHistory)winputs.getDestination()).value().contains(history.getId()));
    	Assert.assertEquals(winputs.getInputs().size(), 1);
    	WorkflowInput winput = (WorkflowInput)winputs.getInputs().values().toArray()[0];
    	Assert.assertEquals(winput.getId(), dataset.getId());
    	Assert.assertEquals(winput.getSourceType(), InputSourceType.LDDA);
    	Assert.assertEquals(winputs.getWorkflowId(), workflow.getId());
    	Assert.assertEquals(winputs.getParameters().size(), 1);    	
    }

    @Test(expected = GalaxyWorkflowException.class)
    public void shouldThrowExceptionBuildnputsForNonExistingWorkflow() {
    	jobService.buildWorkflowInputs("foobar", "", "", new HashMap<String, Map<String, String>>());
    }

    // TODO remove ignore once sample media file is added to repository
    @Ignore
    @Test
    public void shouldCreateJobOnValidInputs() {    	              
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these for this test
    	Workflow workflow = jobService.getWorkflowsClient().getWorkflows().get(0);     	

    	// before creating the job for the first time on the primaryfile, the dataset ID is not set yet
    	Assert.assertNull(primaryfile.getDatasetId());

    	// use the dummy primaryfile we set up for this test
    	WorkflowOutputs woutputs = jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());

    	// now the dataset ID shall be set
    	Assert.assertNotNull(primaryfile.getDatasetId());
    	
    	// returned workflow outputs shall have contents
    	Assert.assertNotNull(woutputs);
    	Assert.assertNotNull(woutputs.getHistoryId());
    	Assert.assertNotNull(woutputs.getOutputIds());
    	
    	// on subsequence workflow invocation on this primaryfile, the same uploaded dataset shall be reused
    	String datasetId = primaryfile.getDatasetId();
    	jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());
    	Assert.assertEquals(primaryfile.getDatasetId(), datasetId);
    }
    
    @Test(expected = StorageException.class)
    public void shouldThrowStorageExceptionCreateJobForNonExistingPrimaryfile() {
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these for this test
    	Workflow workflow = jobService.getWorkflowsClient().getWorkflows().get(0);     	

    	jobService.createJob(workflow.getId(), 0l, new HashMap<String, Map<String, String>>());
    }
    
    // TODO remove ignore once sample media file is added to repository
    @Ignore
    @Test(expected = GalaxyWorkflowException.class)
    public void shouldThrowGalaxyWorkflowExceptionExceptionCreateJobForNonExistingWorkflow() { 	
    	jobService.createJob("0", primaryfile.getId(), new HashMap<String, Map<String, String>>());
    }
    
    // TODO remove ignore once sample media file is added to repository
    @Ignore
    @Test
    public void shouldCreateJobBundle() {    	              
    	// we assume there is at least one workflow existing in Galaxy, and we can use one of these for this test
    	Workflow workflow = jobService.getWorkflowsClient().getWorkflows().get(0);     	

    	// use the dummy bundle we set up for this test
    	List<WorkflowOutputs> woutputsList = jobService.createJobBundle(workflow.getId(), bundle.getId(), new HashMap<String, Map<String, String>>());

    	// only one primaryfile is valid, so only one workflow outputs shall exist in the list returned
    	Assert.assertNotNull(woutputsList);
    	Assert.assertEquals(woutputsList.size(), 1);
    	Assert.assertNotNull(woutputsList.get(0));
    }

    @Test
    public void testListJobs() {
    	
    }
    
}
