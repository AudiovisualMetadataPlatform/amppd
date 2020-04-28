package edu.indiana.dlib.amppd.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
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
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.PasswordTokenRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import edu.indiana.dlib.amppd.service.impl.GalaxyDataServiceImpl;
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
	public static final String TEST_VIDEO = "TestVideo";	// TODO put a small sample TestVideo.mp4 into repository test resources
	public static final String TEST_WORKFLOW = "TestWorkflow";
	public static final String TEST_HMGM_WORKFLOW = "TestHmgmWorkflow";
	public static final String TEST_OUTPUT = "out_file1";
	public static final String TASK_MANAGER = "Jira";	
	
	@Autowired
    private UnitRepository unitRepository;
	
	@Autowired
    private CollectionRepository collectionRepository;
	
	@Autowired
    private ItemRepository itemRepository;
	
	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
    private FileStorageService fileStorageService; 

	@Autowired
	private WorkflowService workflowService;
	
	@Autowired
	private JobService jobService;
	
	@Autowired
	private AmpUserService ampUserService;
	
	@Autowired
	private AmpUserRepository ampUserRepository;
	
	@Autowired
	private PasswordTokenRepository passwordTokenRepository;
	
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
	 * Check whether the specified primaryfile exists in Amppd; if not, upload it from the resource file with a filename same as the primaryfile name, 
	 * and with the specified extension. 
	 * @param workflowName the name of the specified workflow
	 * @param extension the media file extension of the specified primaryfile 
	 * @return the prepared primaryfile
	 */
	public Primaryfile ensurePrimaryfile(String primaryfileName, String extension) {
		String filename = primaryfileName + "." + extension;
		List<Primaryfile> primaryfiles = primaryfileRepository.findByOriginalFilename(filename);
		Primaryfile primaryfile = primaryfiles.size() > 0 ? primaryfiles.get(0): null;

		// if the primaryfile with the filename already exist in DB, just return it 
		if (primaryfile != null) {
			log.info("Primaryfile " + primaryfile.getId() + " already exists and uploaded with " + filename + ", will use it for testing.");
			return primaryfile;
		}

		// otherwise, create a primaryfile with the given name	
		// and set up the parent hierarchy as needed by tile upload file path calculation

		Unit unit = new Unit();
    	unit.setName("Unit for " + primaryfileName);
    	unit.setDescription("unit for tests");	  
    	unit = unitRepository.save(unit);
    	
		Collection collection = new Collection();
		collection.setName("Collection for " + primaryfileName);
		collection.setDescription("collection for tests");  	
		collection.setTaskManager(TASK_MANAGER);  	
    	collection.setUnit(unit);
    	collection = collectionRepository.save(collection);
    	
    	Item item = new Item();
    	item.setName("Item for " + primaryfileName);
    	item.setDescription("item for tests");  	
    	item.setCollection(collection);
    	item = itemRepository.save(item);

		primaryfile = new Primaryfile();
		primaryfile.setName("Primaryfile for " + primaryfileName);
		primaryfile.setDescription("primaryfile for tests");			
    	primaryfile.setItem(item);
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
		Workflow workflow = ensureTestWorkflow();
		List<Invocation> invocations = jobService.listJobs(workflow.getId(), primaryfile.getId());
		if (invocations.size() > 0) {
			// some job has been run on the workflow-primaryfile, just return the first invocation
			log.info("There are already " + invocations.size() + " AMP test jobs existing for Primaryfile " + primaryfile.getId() + " and Workflow " + workflow.getId()
				+ ", will use job " + invocations.get(0).getId() + " in history " + invocations.get(0).getHistoryId() + " for testing.");
			return invocations.get(0);
		}
		else {
			// otherwise run the job once and return the WorkflowOutputs
			return jobService.createJob(workflow.getId(), primaryfile.getId(), new HashMap<String, Map<String, String>>());
		}
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
		
		String username = "pilotuser@iu.edu";
		AmpUser ampUser = ampUserService.getUser(username);
		if(ampUser==null) {
			ampUser = new AmpUser();
			ampUser.setEmail(username);
			ampUser.setUsername(username);
			ampUser.setPassword(username);
			ampUser.setApprove_status(AmpUser.State.ACCEPTED);
			ampUserService.registerAmpUser(ampUser);
		}
		
    	return ampUser;
	}
	
	/*
	 * Delete all users
	 */
	public void deleteAllUsers() {
		passwordTokenRepository.deleteAll();
		ampUserRepository.deleteAll();
	}
	
	public Unit createTestUnit() {
		Unit unit = null;
		String unitName = "AMP Pilot Unit";
		List<Unit> units = unitRepository.findByName(unitName);
		
		if(units.size()>0) {
			unit = units.get(0);
		}
		else {
			unit = new Unit();
			unit.setName(unitName);
			unit.setModifiedBy("testuser");
			unit.setCreatedBy("testuser");
			unit.setModifiedDate(new Date());
			unit.setCreatedDate(new Date());
			unitRepository.save(unit);
		}
		return unit;
	}
	
}
