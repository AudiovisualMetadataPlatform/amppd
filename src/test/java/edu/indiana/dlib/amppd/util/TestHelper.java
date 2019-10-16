package edu.indiana.dlib.amppd.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.service.WorkflowService;
import edu.indiana.dlib.amppd.service.impl.GalaxyDataServiceImpl;
import lombok.extern.java.Log;

/**
 * Class for helper methods facilitating various tests in Amppd.
 * @author yingfeng
 *
 */
@Service
@Log
public class TestHelper {
	
	public static final List<String> AUDIO_TYPES = new ArrayList<>(List.of("mp3", "wav", "m4a", "ogg"));
	public static final List<String> VIDEO_TYPES = new ArrayList<>(List.of("mp4", "mov", "avi", "wmv"));
	public static final String TEST_AUDIO = "TestAudio";
	public static final String TEST_VIDEO = "TestVideo";	// TODO put a small sample TestVideo.mp4 into repository test resources
	public static final String TEST_WORKFLOW = "TestWorkflow";
	
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
	
}
