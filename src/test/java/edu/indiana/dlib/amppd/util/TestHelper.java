package edu.indiana.dlib.amppd.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowDetails;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Dataentity;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Supplement;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.TimedTokenRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.repository.WorkflowResultRepository;
import edu.indiana.dlib.amppd.security.JwtTokenUtil;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.service.WorkflowResultService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import edu.indiana.dlib.amppd.service.impl.GalaxyDataServiceImpl;
import edu.indiana.dlib.amppd.web.CreateJobResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Class for helper methods facilitating various tests in Amppd.
 * @author yingfeng
 *
 */
@Service
@Slf4j
public class TestHelper {
	
	public static final List<String> AUDIO_TYPES = new ArrayList<>(List.of("mp3", "wav", "m4a", "ogg"));
	public static final List<String> VIDEO_TYPES = new ArrayList<>(List.of("mp4", "mov", "avi", "wmv"));
	public static final String TEST_AUDIO = "TestAudio";
	public static final String TEST_VIDEO = "TestVideo";	
	public static final String TEST_EXTERNAL_SOURCE = "TestExternalSource";	
	public static final String TEST_EXTERNAL_ID = "TestExternalId";	
	public static final String TEST_IMAGES = "TestImages";	
	public static final String TEST_WORKFLOW = "TestWorkflow";
	public static final String TEST_WORKFLOW_PUBLISHED = "TestWorkflowPublished";
	public static final String TEST_HMGM_WORKFLOW = "TestHmgmWorkflow";
	public static final String TEST_WORKFLOW_STEP = "remove_trailing_silence"; // the last step in TestWorkflow
	public static final String TEST_OUTPUT = "out_file1";
	public static final String TASK_MANAGER = "Jira";	
	public static final String TEST_USER = "pilotuser@iu.edu";	

	@Autowired
    private JwtTokenUtil tokenUtil;
	
	@Autowired
    private UnitRepository unitRepository;
	
	@Autowired
    private CollectionRepository collectionRepository;
	
	@Autowired
    private ItemRepository itemRepository;
	
	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
	PrimaryfileSupplementRepository primaryfileSupplementRepository;

	@Autowired
	ItemSupplementRepository itemSupplementRepository;

	@Autowired
	CollectionSupplementRepository collectionSupplementRepository;
	
	@Autowired
    private FileStorageService fileStorageService; 

	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private JobService jobService;
	
	@Autowired
    private WorkflowResultService workflowResultService;
	
	@Autowired
	private WorkflowResultRepository workflowResultRepository;  

	@Autowired
	private AmpUserService ampUserService;
	
	@Autowired
	private AmpUserRepository ampUserRepository;
	
	@Autowired
	private TimedTokenRepository timedTokenRepository;
	
	/**
	 * Check whether the primaryfile named TestAudio exists in Amppd; if not, upload it from its resource file.
	 * @return the prepared primaryfile as existing in Amppd 
	 */
	public Primaryfile ensureTestAudio() {
		return ensurePrimaryfile(TEST_AUDIO, "mp3");
	}
	
	/**
	 * Check whether the primaryfile named TestVideo exists in Amppd; if not, upload it from its resource file.
	 * @return the prepared primaryfile as existing in Amppd 
	 */
	public Primaryfile ensureTestVideo() {
		return ensurePrimaryfile(TEST_VIDEO, "mp4");
	}
	
	/**
	 * Check whether the collectionSupplement named TestImages with zip file extension is associated with the given primaryfile in Amppd; 
	 * if not, upload it from its resource file.
	 * @param primaryfile the given primaryfile
	 * @return the prepared collectionSupplement as existing in Amppd 
	 */
	public CollectionSupplement ensureTestCollectionSupplementZip(Primaryfile primaryfile) {
		return (CollectionSupplement)ensureSupplement(primaryfile, TEST_IMAGES, SupplementType.COLLECTION, "zip");
	}
	
	/**
	 * Check whether the workflow named TestWorkflow exists in Galaxy; if not, upload it from its resource file.
	 * @return the prepared workflow as existing in Galaxy 
	 */
	public Workflow ensureTestWorkflow() {
		return ensureWorkflow(TEST_WORKFLOW);
	}
	
	/**
	 * Ensure that the workflow named TestWorkflow exists in Galaxy and returns the details of it.
	 * @return the prepared workflow details as existing in Galaxy 
	 */
	public WorkflowDetails ensureTestWorkflowDetails() {
		Workflow workflow = ensureWorkflow(TEST_WORKFLOW);
		return workflowService.getWorkflowsClient().showWorkflow(workflow.getId());
	}
	
	/**
	 * Check whether the workflow named TestWorkflow exists in Galaxy; if not, upload it from its resource file.
	 * @return the prepared workflow as existing in Galaxy 
	 */
	public Workflow ensureTestWorkflowPublished() {
		return ensureWorkflow(TEST_WORKFLOW_PUBLISHED);
	}
	
	/**
	 * Check whether the workflow named TestHmgmWorkflow exists in Galaxy; if not, upload it from its resource file.
	 * @return the prepared workflow as existing in Galaxy 
	 */
	public Workflow ensureTestHmgmWorkflow() {
		return ensureWorkflow(TEST_HMGM_WORKFLOW);
	}
	
	/**
	 * Ensure that the workflow named TestHmgmWorkflow exists in Galaxy and returns the details of it.
	 * @return the prepared workflow details as existing in Galaxy 
	 */
	public WorkflowDetails ensureTestHmgmWorkflowDetails() {
		Workflow workflow = ensureWorkflow(TEST_HMGM_WORKFLOW);
		return workflowService.getWorkflowsClient().showWorkflow(workflow.getId());
	}
	
	/**
	 * Check whether the specified unit exists in Amppd; if not, create one with the given name. 
	 * @param name name of the specified unit
	 * @return the prepared unit
	 */
	public Unit ensureUnit(String name) {
		// retrieve unit from DB by name
		List<Unit> units = unitRepository.findByName(name);
		Unit unit = units.size() > 0 ? units.get(0): null;

		// if the unit already exists in DB, just return it 
		if (unit != null) {
			return unit;
		}

		// otherwise, create a unit with the given name	
		unit = new Unit();
    	unit.setName(name);
    	unit.setDescription("unit for tests");	  
    	unit = unitRepository.save(unit);
		return unit;
	}
	
	/**
	 * Check whether the specified collection exists in Amppd; if not, create one with the given unit name and name. 
	 * @param unitName name of the specified unit
	 * @param name name of the specified collection
	 * @return the prepared collection
	 */
	public Collection ensureCollection(String unitName, String name) {
		// retrieve collection from DB by unit name and name
		List<Collection> collections = collectionRepository.findByUnitNameAndName(unitName, name);
		Collection collection = collections.size() > 0 ? collections.get(0): null;
		
		// if the collection already exists in DB, just return it 
		if (collection != null) {
			return collection;
		}

		// otherwise, create a collection with the given name	
		collection = new Collection();
		Unit unit = ensureUnit(unitName);
    	collection.setUnit(unit);
    	collection.setName(name);
    	collection.setDescription("collection for tests");
    	collection.setTaskManager("Jira");
    	collection = collectionRepository.save(collection);
		return collection;
	}
		
	/**
	 * Check whether the specified item exists in Amppd; if not, create one with the given unit name, collection name, and name. 
	 * @param unitName name of the specified unit
	 * @param collectionName name of the specified collection
	 * @param name name of the specified item
	 * @return the prepared item
	 */
	public Item ensureItem(String unitName, String collectionName, String name) {
		// retrieve item from DB by unit name, collection name, and name
		List<Item> items = itemRepository.findByCollectionUnitNameAndCollectionNameAndName(unitName, collectionName, name);
		Item item = items.size() > 0 ? items.get(0): null;
		
		// if the item already exists in DB, just return it 
		if (item != null) {
			return item;
		}

		// otherwise, create an item with the given name	
		item = new Item();
		Collection collection = ensureCollection(unitName, collectionName);
    	item.setCollection(collection);
    	item.setName(name);
    	item.setDescription("item for tests");
    	item = itemRepository.save(item);
		return item;
	}
		
	/**
	 * Check whether the specified primaryfile exists in Amppd; if not, create one with the given unit name, collection name, item name, and name.
	 * Note: this primaryfile doesn't have media file uploaded; so it can't be used for workflows but only serves as a container.  
	 * @param unitName name of the specified unit
	 * @param collectionName name of the specified collection
	 * @param itemName name of the specified item
	 * @param name name of the specified primaryfile
	 * @return the prepared primaryfile
	 */
	public Primaryfile ensurePrimaryfile(String unitName, String collectionName, String itemName, String name) {
		// retrieve primaryfile from DB by unit name, collection name, item name, and name
		List<Primaryfile> primaryfiles = primaryfileRepository.findByItemCollectionUnitNameAndItemCollectionNameAndItemNameAndName(unitName, collectionName, itemName, name);
		Primaryfile primaryfile = primaryfiles.size() > 0 ? primaryfiles.get(0): null;
		
		// if the primaryfile already exists in DB, just return it 
		if (primaryfile != null) {
			return primaryfile;
		}

		// otherwise, create a unit with the given name	
		primaryfile = new Primaryfile();
		Item item = ensureItem(unitName, collectionName, itemName);
    	primaryfile.setItem(item);
    	primaryfile.setName(name);
    	primaryfile.setDescription("primaryfile for tests");
    	primaryfile = primaryfileRepository.save(primaryfile);
		return primaryfile;
	}
	
	/**
	 * Check whether the specified primaryfile exists in Amppd; if not, upload it from the resource file with a filename same as the primaryfile name, 
	 * and with the specified extension. 
	 * @param name name of the specified primaryfile
	 * @param extension media file extension of the specified primaryfile 
	 * @return the prepared primaryfile
	 */
	public Primaryfile ensurePrimaryfile(String name, String extension) {
		// retrieve primaryfile from DB via original filename
		String filename = name + "." + extension;
		List<Primaryfile> primaryfiles = primaryfileRepository.findByOriginalFilename(filename);
		Primaryfile primaryfile = primaryfiles.size() > 0 ? primaryfiles.get(0): null;

		// if the primaryfile with the filename already exist in DB, just return it 
		if (primaryfile != null) {
			log.info("Primaryfile " + primaryfile.getId() + " already exists and uploaded with " + filename + ", will use it for testing.");
			return primaryfile;
		}

		// otherwise, create a primaryfile with the given name	
		// and set up the parent hierarchy as needed by file upload file path calculation
		primaryfile = new Primaryfile();
		Item item = ensureItem("Unit for " + name, "Collection for " + name, "Item for " + name);
		primaryfile.setItem(item);
		primaryfile.setName("Primaryfile for " + name);
		primaryfile.setDescription("primaryfile for tests");			
    	primaryfile = primaryfileRepository.save(primaryfile);

		// and upload to it the resource file with the same name
		try {
			MultipartFile file = new MockMultipartFile(filename, filename, getContentType(extension), new ClassPathResource(filename).getInputStream());
			primaryfile = fileStorageService.uploadPrimaryfile(primaryfile, file);
		}
		catch (IOException e) {
			throw new RuntimeException("Unable to create MultipartFile for uploading " + filename + " to primaryfile.", e);
		}

		// return the persisted primaryfile with ID populated 
		log.info("Successfully created primaryfile " + primaryfile.getId() + " and uploaded media for it from resoruce " + filename);
		return primaryfile;
	}

	/**
	 * Check whether the specified supplement exists in Amppd; if not, create one associated with the specified dataentity and name. 
	 * Note: this supplement doesn't have media file uploaded; so it can't be used for workflows but only serves as a container.  
	 * @param dataentity the dataentity associated with the specified supplement
	 * @param name name of the specified supplement
	 * @return the prepared supplement
	 */
	public Supplement ensureSupplement(Dataentity dataentity, String name) {
		List<? extends Supplement> supplements = null;		
		if (dataentity instanceof Collection) {
			supplements = collectionSupplementRepository.findByCollectionIdAndName(dataentity.getId(), name);
		}
		else if (dataentity instanceof Item) {
			supplements = itemSupplementRepository.findByItemIdAndName(dataentity.getId(), name);
		}
		else if (dataentity instanceof Primaryfile) {
			supplements = primaryfileSupplementRepository.findByPrimaryfileIdAndName(dataentity.getId(), name);
		}
		else {
			// if Dataentity is not one of the above types, throw exception
			throw new RuntimeException("Can't create supplement for Dataentity " + dataentity.getId() + " of invalid type.");
		}
		
		// if the supplement already exists in DB, just return it 
		Supplement supplement = supplements.size() > 0 ? supplements.get(0): null;		
		if (supplement != null) {
			return supplement;
		}

		// otherwise, create an supplement with the given name	
		if (dataentity instanceof Collection) {
			CollectionSupplement newSup = new CollectionSupplement();
			newSup.setCollection((Collection)dataentity);
			newSup.setName(name);
			newSup.setDescription("supplement for tests");
			newSup = collectionSupplementRepository.save(newSup);
			return newSup;
		}
		else if (dataentity instanceof Item) {
			ItemSupplement newSup = new ItemSupplement();
			newSup.setItem((Item)dataentity);
			newSup.setName(name);
			newSup.setDescription("supplement for tests");
			newSup = itemSupplementRepository.save(newSup);
			return newSup;
		}
		else if (dataentity instanceof Primaryfile) {
			PrimaryfileSupplement newSup = new PrimaryfileSupplement();
			newSup.setPrimaryfile((Primaryfile)dataentity);
			newSup.setName(name);
			newSup.setDescription("supplement for tests");
			newSup = primaryfileSupplementRepository.save(newSup);
			return newSup;
		}
		
		return null;
	}

	/**
	 * Check whether the supplement with the specified name, type and the associated primaryfile exists in Amppd; if not, 
	 * upload it from the resource file with a filename same as the supplement name and with the specified extension. 
	 * @param primaryfile the specified primaryfile whose ancestor is the parent associated with the supplement 
	 * @param name name of the specified supplement
	 * @param type the association type of the specified supplement to its parent 
	 * @param extension media file extension of the specified supplement 
	 * @return the prepared supplement
	 */
	public Supplement ensureSupplement(Primaryfile primaryfile, String name, SupplementType type, String extension) {
		// retrieve supplement from DB for the given primaryfile, supplement name, based on its association type 
		List<? extends Supplement> supplements = null;
		switch(type) {
		case COLLECTION:
			supplements = collectionSupplementRepository.findByCollectionIdAndName(primaryfile.getItem().getCollection().getId(), name);
			break;
		case ITEM:
			supplements = itemSupplementRepository.findByItemIdAndName(primaryfile.getItem().getId(), name);
			break;
		case PRIMARYFILE:
			supplements = primaryfileSupplementRepository.findByPrimaryfileIdAndName(primaryfile.getId(), name);
			break;
		default:
			throw new RuntimeException("Invalid SupplementType " + type);
		}		

		// if the supplement already exists in DB, just return it 
		Supplement supplement = supplements !=  null && supplements.size() > 0 ? supplements.get(0): null;
		if (supplement != null) {
			log.info(type + " Supplement " + supplement.getId() + " with name " + name + " already exists for primaryfile " + primaryfile.getId() + ", will use it for testing.");
			return supplement;
		}
		
		// otherwise, prepare the resource file name.extension for upload
		String filename = name + "." + extension;
    	MultipartFile file = null;
		try {
			file = new MockMultipartFile(filename, filename, getContentType(extension), new ClassPathResource(filename).getInputStream());
		}
		catch (IOException e) {
			throw new RuntimeException("Unable to create MultipartFile for uploading " + filename + " to supplement.", e);
		}

		// and create a supplement with the given name and type for the associated primaryfile, and upload its resource file	
		switch(type) {
		case COLLECTION:
	    	supplement = new CollectionSupplement();
	    	((CollectionSupplement)supplement).setCollection(primaryfile.getItem().getCollection());
			supplement.setName(name);
			supplement.setDescription(type + " Supplement for tests");			
			supplement = collectionSupplementRepository.save((CollectionSupplement)supplement);
			supplement = fileStorageService.uploadCollectionSupplement((CollectionSupplement)supplement, file);
			break;
		case ITEM:
	    	supplement = new ItemSupplement();
	    	((ItemSupplement)supplement).setItem(primaryfile.getItem());
			supplement.setName(name);
			supplement.setDescription(type + " Supplement for tests");			
			supplement = itemSupplementRepository.save((ItemSupplement)supplement);
			supplement = fileStorageService.uploadItemSupplement((ItemSupplement)supplement, file);
			break;
		case PRIMARYFILE:
	    	supplement = new PrimaryfileSupplement();
	    	((PrimaryfileSupplement)supplement).setPrimaryfile(primaryfile);
			supplement.setName(name);
			supplement.setDescription(type + " Supplement for tests");			
			supplement = primaryfileSupplementRepository.save((PrimaryfileSupplement)supplement);
			supplement = fileStorageService.uploadPrimaryfileSupplement((PrimaryfileSupplement)supplement, file);
			break;
		}			

		// return the persisted supplement with ID populated 
		log.info("Successfully created " + type + " Supplement " + supplement.getId() + " for primaryfile " + primaryfile.getId() + ", and uploaded media for it from resoruce " + filename);		
		return supplement;
	}	
	
	/**
	 * Check whether the specified supplement exists in Amppd; if not, upload it from the resource file with the filename name_type.extension. 
	 * @param name name of the specified supplement
	 * @param type the association type of the specified supplement to its parent 
	 * @param extension media file extension of the specified supplement 
	 * @return the prepared supplement
	 */
	public Supplement ensureSupplement(String name, SupplementType type, String extension) {
		// filename of the resource is name_type.extension
		String filename = name + "_" + type + "." + extension;
		List<? extends Supplement> supplements = null;

		// retrieve supplement from DB via original filename
		switch(type) {
		case COLLECTION:
			supplements = collectionSupplementRepository.findByOriginalFilename(filename);
			break;
		case ITEM:
			supplements = itemSupplementRepository.findByOriginalFilename(filename);
			break;
		case PRIMARYFILE:
			supplements = primaryfileSupplementRepository.findByOriginalFilename(filename);
			break;
		default:
			throw new RuntimeException("Invalid SupplementType " + type);
		}			

		// if the supplement with the filename already exist in DB, just return it 
		Supplement supplement = supplements.size() > 0 ? supplements.get(0): null;
		if (supplement != null) {
			log.info("Supplement " + supplement.getId() + " already exists and uploaded with " + filename + ", will use it for testing.");
			return supplement;
		}

		// otherwise, prepare the parent hierarchy as needed by file upload file path calculation
		Unit unit = new Unit();
    	unit.setName("Unit for " + name);
    	unit.setDescription("unit for tests");	  
    	unit = unitRepository.save(unit);
    	Collection collection = null;
    	Item item = null;
    	Primaryfile primaryfile = null;
    	
    	if (type == SupplementType.COLLECTION || type == SupplementType.ITEM || type == SupplementType.PRIMARYFILE) {
			collection = new Collection();
			collection.setName("Collection for " + name);
			collection.setDescription("collection for tests");  	
			collection.setTaskManager(TASK_MANAGER);  	
	    	collection.setUnit(unit);
	    	collection = collectionRepository.save(collection);
    	
	    	if (type == SupplementType.ITEM || type == SupplementType.PRIMARYFILE) {
	    		item = new Item();
	    		item.setName("Item for " + name);
	    		item.setDescription("item for tests");  
	    		item.setExternalSource(TEST_EXTERNAL_SOURCE);
	    		item.setExternalSource(TEST_EXTERNAL_ID);
	    		item.setCollection(collection);
	    		item = itemRepository.save(item);

	    		if (type == SupplementType.PRIMARYFILE) {
	    			primaryfile = new Primaryfile();
	    			primaryfile.setName("Primaryfile for " + name);
	    			primaryfile.setDescription("primaryfile for tests");			
	    	    	primaryfile.setItem(item);
	    			primaryfile = primaryfileRepository.save(primaryfile);
	    		}
	    	}
    	}    	
		
		// and prepare the resource file with the above filename 
    	MultipartFile file = null;
		try {
			file = new MockMultipartFile(filename, filename, getContentType(extension), new ClassPathResource(filename).getInputStream());
		}
		catch (IOException e) {
			throw new RuntimeException("Unable to create MultipartFile for uploading " + filename + " to supplement.", e);
		}

		// and create a supplement with the given name and the created parent hierarchy, and upload its resource file	
		switch(type) {
		case COLLECTION:
	    	supplement = new CollectionSupplement();
	    	((CollectionSupplement)supplement).setCollection(collection);
			supplement.setName(type + " Supplement for " + name);
			supplement.setDescription(type + " Supplement for tests");						
			supplement = collectionSupplementRepository.save((CollectionSupplement)supplement);
			supplement = fileStorageService.uploadCollectionSupplement((CollectionSupplement)supplement, file);
		case ITEM:
	    	supplement = new ItemSupplement();
	    	((ItemSupplement)supplement).setItem(item);
			supplement.setName(type + " Supplement for " + name);
			supplement.setDescription(type + " Supplement for tests");			
			supplement = itemSupplementRepository.save((ItemSupplement)supplement);
			supplement = fileStorageService.uploadItemSupplement((ItemSupplement)supplement, file);
		case PRIMARYFILE:
	    	supplement = new PrimaryfileSupplement();
	    	((PrimaryfileSupplement)supplement).setPrimaryfile(primaryfile);
			supplement.setName(type + " Supplement for " + name);
			supplement.setDescription(type + " Supplement for tests");			
			supplement = primaryfileSupplementRepository.save((PrimaryfileSupplement)supplement);
			supplement = fileStorageService.uploadPrimaryfileSupplement((PrimaryfileSupplement)supplement, file);
		}			

		// return the persisted supplement with ID populated 
		log.info("Successfully created supplement " + supplement.getId() + " and uploaded media for it from resoruce " + filename);
		return supplement;
	}

	/**
	 * Check whether the specified workflow exists in Galaxy; if not, upload it from the resource file with a filename same as the workflow name.
	 * @param workflowName the name of the specified workflow
	 * @return the prepared workflow
	 */
	public Workflow ensureWorkflow(String workflowName) {
		Workflow workflow = workflowService.getWorkflow(workflowName);
		String filename = workflowName + ".ga";

		// if the workflow with the given name already exists in Galaxy, just return it 
		if (workflow != null) {
			log.info("Workflow " + workflow.getId() + " with name " + workflowName + " already exists, will use it for testing.");
			return workflow;
		}

		// otherwise, load it from the resource file with the same name
		try {
			String workflowContents = Resources.asCharSource(new ClassPathResource(filename).getURL(), Charsets.UTF_8).read();
			// workflowContents = Resources.asCharSource(getClass().getResource(workflowName + ".ga"), Charsets.UTF_8).read();
			workflow = workflowService.getWorkflowsClient().importWorkflow(workflowContents);
		} catch (IOException e) {
			throw new RuntimeException("Unable to upload workflow " + workflowName + " from resource " + filename + " into Galaxy.", e);
		}

		log.info("Successfully uploaded workflow " + workflow.getId() + " from " + filename + " into Galaxy.");
		return workflow;
	}	
	
	/**
	 * Check whether any AMP job has been run on TestWorkflow against TestAudio/TestVideo; if not, prepare the workflow and primaryfile and run the job once.
	 * @param useAudio if true use TestAudio, otherwise use TestVideo as the primaryfile
	 * @return the prepared invocation 
	 */
	public Invocation ensureTestJob(boolean useAudio) {				
		Primaryfile primaryfile = useAudio ? ensureTestAudio() : ensureTestVideo();
		WorkflowDetails workflowDetails = ensureTestWorkflowDetails();
		List<Invocation> invocations = jobService.listJobs(workflowDetails.getId(), primaryfile.getId());
		if (invocations.size() > 0) {
			// some job has been run on the workflow-primaryfile, just return the first invocation
			log.info("There are already " + invocations.size() + " AMP test jobs existing for Primaryfile " + primaryfile.getId() + " and Workflow " + workflowDetails.getId()
				+ ", will use job " + invocations.get(0).getId() + " in history " + invocations.get(0).getHistoryId() + " for testing.");
			return invocations.get(0);
		}
		else {
			// otherwise run the job once and return the WorkflowOutputs
			CreateJobResponse result = jobService.createJob(workflowDetails, primaryfile.getId(), new HashMap<String, Map<String, String>>());
			invocations = jobService.listJobs(workflowDetails.getId(), primaryfile.getId());
			return invocations.get(0);
		}
	}	
	
	/**
	 * Check whether Workflow Results has been populated with test workflow invocation results for test primaryfile; 
	 * if not, run test workflow on it and add the output to WorkflowResults with the last step result set as final.
	 * @param useAudio if true use TestAudio, otherwise use TestVideo as the primaryfile
	 * @return the prepared list of WorkflowResults 
	 */
	public List<WorkflowResult> ensureTestWorkflowResults(boolean useAudio) {	
		Primaryfile primaryfile = useAudio ? ensureTestAudio() : ensureTestVideo();
    	Workflow workflow = ensureTestWorkflow();
    	Invocation invocation = ensureTestJob(useAudio);
    	
		List<WorkflowResult> results = workflowResultRepository.findByPrimaryfileId(primaryfile.getId());
		if (!results.iterator().hasNext()) {
			workflowResultService.addWorkflowResults(invocation, workflow, primaryfile);
			results = workflowResultRepository.findByPrimaryfileId(primaryfile.getId());
		}
			
		for (WorkflowResult result : results) {
			if (result.getWorkflowStep().equals(TEST_WORKFLOW_STEP) && result.getInvocationId().equals(invocation.getId())) {
				result.setIsFinal(true);
				workflowResultRepository.save(result);
			}
		}
		
		return results;
	}	
	
	/**
	 * Return the standard media content type representation based on the given file extension, or null if the extension is not one of the common video/audio formats.
	 * @param extention
	 * @return
	 */
	public String getContentType(String extension) {
		if (StringUtils.isEmpty(extension)) {
			return null;
		}		
		String extlow =  extension.toLowerCase();
		String contentType = VIDEO_TYPES.contains(extlow) ? "video" : AUDIO_TYPES.contains(extlow) ? "audio" : null;
		return contentType == null ? null : contentType + "/" + extension;	
	}
	
	/**
	 * Delete all collections. 
	 * Call this method to make sure that the test will use a freshly created collection instead of reusing those created in previous tests. 
	 */
	public void cleanupCollections() {
		collectionRepository.deleteAll();
	}

	/**
	 * Delete all primaryfiles. 
	 * Call this method to make sure that the test will use a freshly created primaryfile instead of reusing those created in previous tests. 
	 */
	public void cleanupPrimaryfiles() {
		primaryfileRepository.deleteAll();
	}

	/**
	 * Delete all histories except the AMPPD shared history, so that temporary histories used for running workflows won't keep building up. 
	 */
	public void cleanupHistories() {
		List<History> histories = jobService.getHistoriesClient().getHistories();
		
		for (History history : histories) {
			if (!history.getName().equals(GalaxyDataServiceImpl.SHARED_HISTORY_NAME)) {
				jobService.getHistoriesClient().deleteHistory(history.getId());
				log.info("History is deleted: ID: " + history.getId() + ", Name: " + history.getName());
			}
		}
	}

	/**
	 * Delete all test workflows.  
	 */
	public void cleanupWorkflows() {
		List<Workflow> workflows = workflowService.getWorkflowsClient().getWorkflows();
		
		for (Workflow workflow : workflows) {
			if (workflow.getName().equals(TEST_WORKFLOW) || workflow.getName().equals(TEST_HMGM_WORKFLOW)) {
				workflowService.getWorkflowsClient().deleteWorkflowRequest(workflow.getId());
				log.info("Workflow is deleted: ID: " + workflow.getId() + ", Name: " + workflow.getName());
			}
		}
	}
	
	/*
	 * Create a test user
	 */
	public AmpUser createTestUser() {	
		AmpUser ampUser = ampUserService.getUser(TEST_USER);
		if(ampUser==null) {
			ampUser = new AmpUser();
			ampUser.setEmail(TEST_USER);
			ampUser.setUsername(TEST_USER);
			ampUser.setPassword(TEST_USER);
			ampUser.setFirstName("AMP");
			ampUser.setLastName("USER");
			ampUser.setStatus(AmpUser.State.ACCEPTED);
			ampUserService.registerAmpUser(ampUser);
		}
		ampUser.setPassword(TEST_USER);
		
    	return ampUser;
	}
	
	public String getToken() {
		AmpUser user = createTestUser();
		return tokenUtil.generateToken(user);
	}
	
	/*
	 * Delete all users
	 */
	public void deleteAllUsers() {
		timedTokenRepository.deleteAll();
		ampUserRepository.deleteAll();
	}
	
	public Unit createTestUnit() {
		Unit unit = null;
		String unitName = "AMP Pilot Unit";
		List<Unit> units = unitRepository.findByName(unitName);
		
		if(units.size()>0) {
			unit = units.get(0);
			log.info("Found existing unit with ID " + unit.getId());
		}
		else {
			unit = new Unit();
			unit.setName(unitName);
			unit = unitRepository.save(unit);
			log.info("Created new unit with ID " + unit.getId());
		}
		return unit;
	}
	
	public Collection createTestCollection() {		
		Unit unit = createTestUnit();
		
		Collection collection = null;
		String collectionName = "AMP Pilot Collection";
		List<Collection> collections = collectionRepository.findByName(collectionName);
		
		if(collections.size()>0) {
			collection = collections.get(0);
			log.info("Found existing collection with ID " + unit.getId());
		}
		else {
			collection = new Collection();
			collection.setName(collectionName);
			collection.setUnit(unit);			
			collection = collectionRepository.save(collection);
			log.info("Created new collection with ID " + collection.getId());
		}
		return collection;
	}
	

}
