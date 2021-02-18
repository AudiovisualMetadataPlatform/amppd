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
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.BundleRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepositoryCustomImpl;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.web.CreateJobResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultResponse;
import edu.indiana.dlib.amppd.web.WorkflowResultSearchQuery;
import edu.indiana.dlib.amppd.web.WorkflowResultSortRule;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowResultServiceTests {

	@MockBean
    private BundleRepository bundleRepository;

	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	@Autowired
    private WorkflowResultService workflowResultService;

	@Autowired
	private JobService jobService;  
	@Autowired
	private WorkflowResultRepository dashboardRepository;    
		
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
		workflowResultService.refreshWorkflowResultsLumpsum();
		
		CreateJobResponse r = 
    			jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());

		WorkflowOutputs woutputs = r.getOutputs();
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
    	
    	
		Iterable<WorkflowResult> results = dashboardRepository.findAll();
	
		for(WorkflowResult result : results) {
    		validateResult(result);
    	}
		
		boolean invocationFound = dashboardRepository.invocationExists(invocation.getId());
		Assert.assertTrue(invocationFound);
		
	}
	@Test
	public void shouldReturnRows() {

		CreateJobResponse r = 
    			jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());

		WorkflowOutputs woutputs = r.getOutputs();
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
    	
    	
    	WorkflowResultSearchQuery query = new WorkflowResultSearchQuery();
    	query.setPageNum(1);
    	query.setResultsPerPage(5);
    	WorkflowResultSortRule sort = new WorkflowResultSortRule();
    	sort.setColumnName(WorkflowResultRepositoryCustomImpl.DATE_PROPERTY);
    	
    	query.setSortRule(sort);
    	
    	WorkflowResultResponse results = workflowResultService.getWorkflowResults(query);
    	List<WorkflowResult> records = results.getRows();
    	
    	Assert.assertTrue(!records.isEmpty());
    	boolean returnedPrimaryFile = false;
    	for(WorkflowResult result : records) {
    		validateResult(result);
    		if(result.getPrimaryfileName().equals(pf1.getOriginalFilename()) &&
    				result.getItemName().equals(pf1.getItem().getName())) {
    			returnedPrimaryFile = true;
    		}
    	}
    	Assert.assertTrue(returnedPrimaryFile);
    	
	}
	private void validateResult(WorkflowResult result) {
		Assert.assertNotNull(result.getOutputName());
    	Assert.assertNotNull(result.getItemName());
    	Assert.assertNotNull(result.getPrimaryfileName());
    	Assert.assertNotNull(result.getSubmitter());
    	Assert.assertNotNull(result.getWorkflowName());
    	Assert.assertNotNull(result.getWorkflowStep());
    	Assert.assertNotNull(result.getStatus());
    	Assert.assertNotNull(result.getDateCreated());
	}
	
	// TODO add tests for other public methods in WorkflowResultService.
	
}
