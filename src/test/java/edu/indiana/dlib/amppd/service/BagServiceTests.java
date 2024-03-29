package edu.indiana.dlib.amppd.service;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemBag;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileBag;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.util.TestHelper;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class BagServiceTests {

	@Autowired
    private BagService bagService;
	
	@Autowired
	private TestHelper testHelper;   

	private Primaryfile primaryfile;
	private Workflow workflow;
	private Invocation invocation;
//	private List<WorkflowResult> results;

	/**
	 * Initialize Dashboard.
	 */
	@Before
	public void setup() {
		// prepare the primaryfile, workflow, workflow invocation, and dashboard for testing
		primaryfile = testHelper.ensureTestAudio();
		workflow = testHelper.ensureTestWorkflow();
		invocation = testHelper.ensureTestJob(true);
//		results = testHelper.ensureTestWorkflowResults(true);		
	}
	
	@Test
	public void shouldGetPrimaryfileBag() {
		PrimaryfileBag pbag = bagService.getPrimaryfileBag(primaryfile.getId());
		Assert.assertEquals(pbag.getPrimaryfileId(), primaryfile.getId());
		Assert.assertEquals(pbag.getPrimaryfileName(), primaryfile.getName());

		List<BagContent> contents = pbag.getBagContents();
		Assert.assertEquals(contents.size(), 1);	// only the TEST_WORKFLOW_STEP output is final

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
		Assert.assertEquals(ibag.getItemId(), item.getId());
		Assert.assertEquals(ibag.getItemName(), item.getName());

		List<PrimaryfileBag> pbags = ibag.getPrimaryfileBags();
		Assert.assertEquals(pbags.size(), 1);	// primaryfile is the only child of its parent item

		PrimaryfileBag pbag = pbags.get(0);
		Assert.assertEquals(pbag.getPrimaryfileId(), primaryfile.getId());		
	}
	
	@Test
	public void shouldGetItemBagByExternalId() {
		Item item = primaryfile.getItem();		
		
		ItemBag ibag = bagService.getItemBag(item.getExternalSource(), item.getExternalId());
		Assert.assertEquals(ibag.getItemId(), item.getId());
		Assert.assertEquals(ibag.getExternalSource(), item.getExternalSource());
		Assert.assertEquals(ibag.getExternalId(), item.getExternalId());

		List<PrimaryfileBag> pbags = ibag.getPrimaryfileBags();
		Assert.assertEquals(pbags.size(), 1);

		PrimaryfileBag pbag = pbags.get(0);
		Assert.assertEquals(pbag.getPrimaryfileId(), primaryfile.getId());		
	}
	
	@Test
	public void shouldGetCollectionBagById() {
		Item item = primaryfile.getItem();		
		Collection collection = item.getCollection();		
		
		CollectionBag cbag = bagService.getCollectionBag(collection.getId());
		Assert.assertEquals(cbag.getCollectionId(), collection.getId());
		Assert.assertEquals(cbag.getCollectionName(), collection.getName());

		List<ItemBag> ibags = cbag.getItemBags();
		Assert.assertEquals(ibags.size(), 1);	// item is the only child of its parent collection

		ItemBag ibag = ibags.get(0);
		Assert.assertEquals(ibag.getItemId(), item.getId());		
	}
	
	@Test
	public void shouldGetCollectionBagByName() {
		Item item = primaryfile.getItem();		
		Collection collection = item.getCollection();		
		Unit unit = collection.getUnit();		
		
		CollectionBag cbag = bagService.getCollectionBag(unit.getName(), collection.getName());
		Assert.assertEquals(cbag.getCollectionId(), collection.getId());
		Assert.assertEquals(cbag.getUnitName(), unit.getName());
		Assert.assertEquals(cbag.getCollectionName(), collection.getName());

		List<ItemBag> ibags = cbag.getItemBags();
		Assert.assertEquals(ibags.size(), 1);

		ItemBag ibag = ibags.get(0);
		Assert.assertEquals(ibag.getItemId(), item.getId());		
	}
	

}
