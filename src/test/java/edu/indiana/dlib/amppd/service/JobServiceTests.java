package edu.indiana.dlib.amppd.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
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
import org.springframework.transaction.annotation.Transactional;

import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationDetails;
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
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.util.TestHelper;

// TODO remove ignore once we have Galaxy Bootstrap working on Bamboo
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class JobServiceTests {

	public static final Long BUNDLE_ID = 2l;

	@MockBean
    private BundleRepository bundleRepository;

	@Autowired
    private PrimaryfileRepository primaryfileRepository;
		
	@Autowired
	private JobService jobService;   
		
	@Autowired
	private TestHelper testHelper;   
	
	private Primaryfile primaryfile;
	private Workflow workflow;
	private Invocation invocation;
	
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
	}
	
		
	@After
	public void cleanup() {
//		testHelper.cleanupHistories();
	}
		    	
    @Test
    public void shouldBuildWorkflowInputsOnValidInputs() {    	
    	// set up some dummy history and dataset
    	History history = new History();
    	history.setId("1");
    	LibraryContent dataset = new LibraryContent();
    	dataset.setId("1");

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

    @Test
    public void shouldCreateJobOnValidInputs() {    	              
    	WorkflowOutputs woutputs = invocation instanceof WorkflowOutputs ? 
    			(WorkflowOutputs)invocation  :
    			jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());

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
    	jobService.createJob(workflow.getId(), 0l, new HashMap<String, Map<String, String>>());
    }
    
    @Test(expected = GalaxyWorkflowException.class)
    public void shouldThrowGalaxyWorkflowExceptionExceptionCreateJobForNonExistingWorkflow() { 	
    	jobService.createJob("0", primaryfile.getId(), new HashMap<String, Map<String, String>>());
    }
    
    @Test
    @Transactional
    public void shouldCreateJobBundle() {    	               	
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
    	Map<Long, WorkflowOutputs> woutputsMap = jobService.createJobBundle(workflow.getId(), bundle.getId(), new HashMap<String, Map<String, String>>());

    	// only one primaryfile is valid, so only one workflow outputs shall exist in the list returned
    	Assert.assertNotNull(woutputsMap);
    	Assert.assertEquals(woutputsMap.size(), 1);
    	Assert.assertTrue(woutputsMap.containsKey(primaryfile.getId()));
    	Assert.assertNotNull(woutputsMap.get(primaryfile.getId()));
    }
    
    @Test
    public void shouldListJobs() {
    	// before running any AMP job on the workflow-primaryfile, record the current number of invocations
    	List<Invocation> invocations = jobService.listJobs(workflow.getId(), primaryfile.getId());
    	int size = invocations.size();
    	    	
    	// after running the AMP job once on the workflow-primaryfile, there shall be one more invocation listed for this combo
    	jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());
    	invocations = jobService.listJobs(workflow.getId(), primaryfile.getId());
    	Assert.assertEquals(invocations.size(), size + 1);
    	
    	// and the historyId stored in the updated primaryfile shall be the same as that in the invocation
    	Primaryfile updatedPrimaryfile = primaryfileRepository.findById(primaryfile.getId()).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfile.getId() + "> does not exist!"));
    	Assert.assertEquals(invocations.get(0).getHistoryId(), updatedPrimaryfile.getHistoryId());
    	Assert.assertNotNull(invocations.get(0).getId());    	
    	Assert.assertNotNull(invocations.get(0).getUpdateTime());    	
    	Assert.assertNotNull(invocations.get(0).getState()); // this assertion succeeds when running this test alone but fails when running the whole test class   
    }
    
    @Test(expected = StorageException.class)
    public void shouldThrowExceptionListJobsOnNonExistingPrimaryfile() {
    	jobService.listJobs(workflow.getId(), 0L);
    }
    
    @Test(expected = GalaxyWorkflowException.class)
    public void shouldThrowExceptionListJobsOnNonExistingWorkflow() {
    	jobService.listJobs("foobar", primaryfile.getId());
    }
           
    @Test
    public void shouldShowJobStepOutput() {
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
    	Dataset dataset = jobService.showJobStepOutput(workflow.getId(), invocation.getId(), stepId, datasetId);
    	
    	// verify the fields
    	Assert.assertEquals(dataset.getId(), datasetId);
    	Assert.assertEquals(dataset.getHistoryId(), invocation.getHistoryId());
       	Assert.assertNotNull(dataset.getFileName());
       	Assert.assertNotNull(dataset.getCreatingJob());
       	Assert.assertNotNull(dataset.getCreateTime());
       	Assert.assertNotNull(dataset.getUpdateTime());
    }
    
    @Test(expected = GalaxyWorkflowException.class)
    public void shouldThrowExceptionShowNonExistingDataset() {
    	jobService.showJobStepOutput(workflow.getId(), "foo", "bar", "foobar");
    }
           
}
