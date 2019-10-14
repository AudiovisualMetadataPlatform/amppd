package edu.indiana.dlib.amppd.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
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

	public static final Long BUNDLE_ID = 2l;

	@MockBean
    private BundleRepository bundleRepository;

	@Autowired
    private PrimaryfileRepository primaryfileRepository;
		
	@Autowired
	private JobService jobService;   
		
	@Autowired
	private TestHelper testHelper;   
		    	
    @Test
    public void shouldBuildWorkflowInputsOnValidInputs() {    	
    	// prepare the workflow for testing
    	Workflow workflow = testHelper.ensureTestWorkflow();    
    	
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
    	// prepare the primaryfile and workflow for testing
    	Primaryfile primaryfile = testHelper.ensureTestAudio();
    	Workflow workflow = testHelper.ensureTestWorkflow();        	
    	
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
    	// prepare the workflow for testing
    	Workflow workflow = testHelper.ensureTestWorkflow();    

    	jobService.createJob(workflow.getId(), 0l, new HashMap<String, Map<String, String>>());
    }
    
    @Test(expected = GalaxyWorkflowException.class)
    public void shouldThrowGalaxyWorkflowExceptionExceptionCreateJobForNonExistingWorkflow() { 	
    	// prepare the primaryfile for testing
    	Primaryfile primaryfile = testHelper.ensureTestAudio();
    	jobService.createJob("0", primaryfile.getId(), new HashMap<String, Map<String, String>>());
    }
    
    @Test
    @Transactional
    public void shouldCreateJobBundle() {    	              
    	// prepare the primaryfile and workflow for testing
    	Primaryfile primaryfile = testHelper.ensureTestAudio();
    	Workflow workflow = testHelper.ensureTestWorkflow();    
    	
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
    	List<WorkflowOutputs> woutputsList = jobService.createJobBundle(workflow.getId(), bundle.getId(), new HashMap<String, Map<String, String>>());

    	// only one primaryfile is valid, so only one workflow outputs shall exist in the list returned
    	Assert.assertNotNull(woutputsList);
    	Assert.assertEquals(woutputsList.size(), 1);
    	Assert.assertNotNull(woutputsList.get(0));
    }

    
    @Test
    public void shouldListJobs() {
    	// prepare the primaryfile and workflow for testing
    	Primaryfile primaryfile = testHelper.ensureTestAudio();
    	Workflow workflow = testHelper.ensureTestWorkflow();    
    	
    	// before running any AMP job on the workflow-primaryfile, there shall be no invocation for this combo	
    	List<Invocation> invocations = jobService.listJobs(workflow.getId(), primaryfile.getId());
    	Assert.assertEquals(invocations.size(), 0);
    	    	
    	// after running the AMP job once on the workflow-primaryfile, there shall be one invocation listed for this combo
    	jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());
    	invocations = jobService.listJobs(workflow.getId(), primaryfile.getId());
    	Assert.assertEquals(invocations.size(), 1);
    	
    	// and the historyId stored in the updated primaryfile shall be the same as that in the invocation
    	Primaryfile updatedPrimaryfile = primaryfileRepository.findById(primaryfile.getId()).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfile.getId() + "> does not exist!"));
    	Assert.assertEquals(invocations.get(0).getHistoryId(), updatedPrimaryfile.getHistoryId());
    	Assert.assertNotNull(invocations.get(0).getId());    	
    	Assert.assertNotNull(invocations.get(0).getUpdateTime());    	
    	Assert.assertNotNull(invocations.get(0).getState());    	
    }
    
    @Test(expected = StorageException.class)
    public void shouldThrowExceptionListJobsOnInvalidPrimaryfile() {
    	// prepare the workflow for testing
    	Workflow workflow = testHelper.ensureTestWorkflow();    	

    	// then list AMP jobs on a non-existing primaryfile
    	jobService.listJobs(workflow.getId(), 0L);
    }
    
    @Test(expected = GalaxyWorkflowException.class)
    public void shouldThrowExceptionListJobsOnInvalidWorkflow() {
    	// prepare the primaryfile for testing
    	Primaryfile primaryfile = testHelper.ensureTestAudio();
  	
    	// then list AMP jobs on a non-existing workflow
    	jobService.listJobs("foobar", primaryfile.getId());
    }
           
}
