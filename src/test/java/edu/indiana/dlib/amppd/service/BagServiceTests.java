package edu.indiana.dlib.amppd.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;

import edu.indiana.dlib.amppd.model.BagContent;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionBag;
import edu.indiana.dlib.amppd.model.DashboardResult;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemBag;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileBag;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.DashboardRepository;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BagServiceTests {

	@Autowired
    private BagService bagService;

	@Autowired
	private DashboardRepository dashboardRepository;    
	
	@Autowired
	private TestHelper testHelper;   

	private Primaryfile primaryfile;
	private Workflow workflow;
	private Invocation invocation;
	List<DashboardResult> results;

	/**
	 * Initialize Dashboard.
	 */
	@Before
	public void setup() {
		// prepare the primaryfile, workflow, workflow invocation, and dashboard for testing
		primaryfile = testHelper.ensureTestAudio();
		workflow = testHelper.ensureTestWorkflow();
		invocation = testHelper.ensureTestJob(true);
		List<DashboardResult> results = testHelper.ensureTestDashboard(true);		
	}
	
	@Test
	public void shouldGetPrimaryfileBag() {
		PrimaryfileBag pbag = bagService.getPrimaryfileBag(primaryfile.getId());
		Assert.assertTrue(pbag.getPrimaryfileId() == primaryfile.getId());
		Assert.assertTrue(pbag.getPrimaryfileName() == primaryfile.getName());

		List<BagContent> contents = pbag.getBagContents();
		Assert.assertTrue(contents.size() == 1);

		BagContent content = contents.get(0);
		Assert.assertEquals(content.getInvocationId(), invocation.getId());
		Assert.assertEquals(content.getWorkflowName(), workflow.getName());
		Assert.assertEquals(content.getWorkflowStep(), TestHelper.TEST_WORKFLOW_STEP);
		Assert.assertTrue(content.getOutputUrl().contains(content.getResultId().toString()));
	}
	
	@Test
	public void shouldGetItemBagById() {
		Item item = primaryfile.getItem();		
		
		ItemBag ibag = bagService.getItemBag(item.getId());
		Assert.assertTrue(ibag.getItemId() == item.getId());
		Assert.assertTrue(ibag.getItemName() == item.getName());

		List<PrimaryfileBag> pbags = ibag.getPrimaryfileBags();
		Assert.assertTrue(pbags.size() == 1);

		PrimaryfileBag pbag = pbags.get(0);
		Assert.assertEquals(pbag.getPrimaryfileId(), primaryfile.getId());		
	}
	
	@Test
	public void shouldGetItemBagByExternalId() {
		Item item = primaryfile.getItem();		
		
		ItemBag ibag = bagService.getItemBag(item.getExternalSource(), item.getExternalId());
		Assert.assertTrue(ibag.getItemId() == item.getId());
		Assert.assertTrue(ibag.getExternalSource() == item.getExternalSource());
		Assert.assertTrue(ibag.getExternalId() == item.getExternalId());

		List<PrimaryfileBag> pbags = ibag.getPrimaryfileBags();
		Assert.assertTrue(pbags.size() == 1);

		PrimaryfileBag pbag = pbags.get(0);
		Assert.assertEquals(pbag.getPrimaryfileId(), primaryfile.getId());		
	}
	
	@Test
	public void shouldGetCollectionBagById() {
		Item item = primaryfile.getItem();		
		Collection collection = item.getCollection();		
		
		CollectionBag cbag = bagService.getCollectionBag(collection.getId());
		Assert.assertTrue(cbag.getCollectionId() == collection.getId());
		Assert.assertTrue(cbag.getCollectionName() == collection.getName());

		List<ItemBag> ibags = cbag.getItemBags();
		Assert.assertTrue(ibags.size() == 1);

		ItemBag ibag = ibags.get(0);
		Assert.assertEquals(ibag.getItemId(), item.getId());		
	}
	
	@Test
	public void shouldGetCollectionBagByName() {
		Item item = primaryfile.getItem();		
		Collection collection = item.getCollection();		
		Unit unit = collection.getUnit();		
		
		CollectionBag cbag = bagService.getCollectionBag(unit.getName(), collection.getName());
		Assert.assertTrue(cbag.getCollectionId() == collection.getId());
		Assert.assertTrue(cbag.getUnitName() == unit.getName());

		List<ItemBag> ibags = cbag.getItemBags();
		Assert.assertTrue(ibags.size() == 1);

		ItemBag ibag = ibags.get(0);
		Assert.assertEquals(ibag.getItemId(), item.getId());		
	}
	

}
