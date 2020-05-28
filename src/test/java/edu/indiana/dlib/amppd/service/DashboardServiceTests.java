package edu.indiana.dlib.amppd.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.DashboardRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.web.DashboardResult;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DashboardServiceTests {

	@MockBean
    private BundleRepository bundleRepository;

	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	@Autowired
    private DashboardService dashboardService;

	@Autowired
	private JobService jobService;  
	@Autowired
	private DashboardRepository dashboardRepository;    
		
	@Autowired
	private TestHelper testHelper;   
	
	private Primaryfile primaryfile;
	private Workflow workflow;
	private Invocation invocation;
	
		
	/**
	 * Initialize the JobServiceImpl bean.
	 */
	@Before
	public void setup() {
    	// prepare the primaryfile, workflow, and the AMP job for testing
    	primaryfile = testHelper.ensureTestAudio();
    	workflow = testHelper.ensureTestWorkflow();
    	invocation = testHelper.ensureTestJob(true);
	}
	@Test
	public void shouldFillDashboardTable() {
		dashboardService.refreshAllDashboardResults();
		
		WorkflowOutputs woutputs = invocation instanceof WorkflowOutputs ? 
    			(WorkflowOutputs)invocation  :
    			jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());

    	// now the dataset ID and history ID shall be set
		Primaryfile pf = primaryfileRepository.findById(primaryfile.getId()).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfile.getId() + "> does not exist!"));
    	Assert.assertNotNull(pf.getDatasetId());
    	Assert.assertNotNull(pf.getHistoryId());
    	
    	// returned workflow outputs shall have contents
    	Assert.assertNotNull(woutputs);
    	Assert.assertNotNull(woutputs.getHistoryId());
    	Assert.assertEquals(woutputs.getHistoryId(), pf.getHistoryId());
    	Assert.assertNotNull(woutputs.getOutputIds());
    	
    	// on subsequence workflow invocation on this primaryfile, the same uploaded dataset shall be reused
    	jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());
		Primaryfile pf1 = primaryfileRepository.findById(primaryfile.getId()).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfile.getId() + "> does not exist!"));
    	Assert.assertEquals(pf1.getDatasetId(), pf.getDatasetId());
    	Assert.assertEquals(pf1.getHistoryId(), pf.getHistoryId());
    	
    	
		Iterable<DashboardResult> results = dashboardRepository.findAll();
	
		for(DashboardResult result : results) {
    		validateResult(result);
    	}
		
		boolean invocationFound = dashboardRepository.invocationExists(invocation.getId());
		Assert.assertTrue(invocationFound);
		
	}
	@Test
	public void shouldReturnRows() {
		WorkflowOutputs woutputs = invocation instanceof WorkflowOutputs ? 
    			(WorkflowOutputs)invocation  :
    			jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());

    	// now the dataset ID and history ID shall be set
		Primaryfile pf = primaryfileRepository.findById(primaryfile.getId()).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfile.getId() + "> does not exist!"));
    	Assert.assertNotNull(pf.getDatasetId());
    	Assert.assertNotNull(pf.getHistoryId());
    	
    	// returned workflow outputs shall have contents
    	Assert.assertNotNull(woutputs);
    	Assert.assertNotNull(woutputs.getHistoryId());
    	Assert.assertEquals(woutputs.getHistoryId(), pf.getHistoryId());
    	Assert.assertNotNull(woutputs.getOutputIds());
    	
    	// on subsequence workflow invocation on this primaryfile, the same uploaded dataset shall be reused
    	jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());
		Primaryfile pf1 = primaryfileRepository.findById(primaryfile.getId()).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfile.getId() + "> does not exist!"));
    	Assert.assertEquals(pf1.getDatasetId(), pf.getDatasetId());
    	Assert.assertEquals(pf1.getHistoryId(), pf.getHistoryId());
    	
    	List<DashboardResult> records = dashboardService.getDashboardResults();
    	
    	Assert.assertTrue(!records.isEmpty());
    	boolean returnedPrimaryFile = false;
    	for(DashboardResult result : records) {
    		validateResult(result);
    		if(result.getSourceFilename().equals(pf1.getOriginalFilename()) &&
    				result.getSourceItem().equals(pf1.getItem().getName())) {
    			returnedPrimaryFile = true;
    		}
    	}
    	Assert.assertTrue(returnedPrimaryFile);
    	
	}
	private void validateResult(DashboardResult result) {
		Assert.assertNotNull(result.getOutputFile());
    	Assert.assertNotNull(result.getSourceItem());
    	Assert.assertNotNull(result.getSourceFilename());
    	Assert.assertNotNull(result.getSubmitter());
    	Assert.assertNotNull(result.getWorkflowName());
    	Assert.assertNotNull(result.getWorkflowStep());
    	Assert.assertNotNull(result.getStatus());
    	Assert.assertNotNull(result.getDate());
	}
	
	
}
