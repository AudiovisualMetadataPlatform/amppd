package edu.indiana.dlib.amppd.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.GalaxyDataService;
import edu.indiana.dlib.amppd.service.WorkflowService;

/**
 * Class for helper methods facilitating various tests in Amppd.
 * @author yingfeng
 *
 */
public class TestHelper {
	
	public static final List<String> AUDIO_TYPES = new ArrayList<>(List.of("mp3", "wav", "m4a", "ogg"));
	public static final List<String> VIDEO_TYPES = new ArrayList<>(List.of("mp4", "mov", "avi", "wmv"));
	public static final String TEST_AUDIO = "TestAudio";
	public static final String TEST_VIDEO = "TestVideo";
	public static final String TEST_WORKFLOW = "TestWorkflow";
	
	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
    private FileStorageService fileStorageService; 

	@Autowired
	private WorkflowService workflowService;
	
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
	 * @return the specified primaryfile as existing in Amppd 
	 */
	public Primaryfile ensurePrimaryfile(String primaryfileName, String extension) {
		String filename = primaryfileName + "." + extension;
		Primaryfile primaryfile = primaryfileRepository.findByOriginalFilename(filename).get(0);
		
		// if the primaryfile doesn't exist in DB, create one
		if (primaryfile == null) {
	    	primaryfile = new Primaryfile();
	    	primaryfile.setName(primaryfileName);
	    	primaryfile.setDescription("primary file for unit test");	
	    	primaryfileRepository.save(primaryfile);
		}
		
		// if the primaryfile doesn't have media file uploaded, upload the resource file
		if (primaryfile.getPathname() == null) {
			try {
				MultipartFile file = new MockMultipartFile(filename, filename, getContentType(extension), new ClassPathResource(filename).getInputStream());
				fileStorageService.uploadPrimaryfile(primaryfile, file);
			}
			catch (IOException e) {
				throw new RuntimeException("Unable to create MultipartFile for uploading " + primaryfileName + "." + extension);
			}
		}
		
		return primaryfile;
	}

	/**
	 * Check whether the specified workflow exists in Galaxy; if not, upload it from the resource file with a filename same as the workflow name.
	 * @param workflowName the name of the specified workflow
	 * @return the specified workflow as existing in Galaxy 
	 */
	public Workflow ensureWorkflow(String workflowName) {
		Workflow workflow = workflowService.getWorkflow(workflowName);
		
		// the workflow doesn't exist in Galaxy, load it from the resource file with the same name.
		if (workflow == null) {
			String workflowContents;
			try {
				workflowContents = Resources.asCharSource(new ClassPathResource(workflowName + ".ga").getURL(), Charsets.UTF_8).read();
//				workflowContents = Resources.asCharSource(getClass().getResource(workflowName + ".ga"), Charsets.UTF_8).read();
			} catch (IOException e) {
				throw new RuntimeException("Unable to upload workflow " + workflowName + " from resource file into Galaxy.", e);
			}
			workflow = workflowService.getWorkflowsClient().importWorkflow(workflowContents);
		}
		
		return workflow;
	}	
	
	/**
	 * Returns the standard media content type representation based on the given file extension, or null if the extension is not one of the common video/audio formats.
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
	

}
