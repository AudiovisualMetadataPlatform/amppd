package edu.indiana.dlib.amppd.util;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

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
	
	public static final String TEST_WORKFLOW = "TestWorkflow";
	
	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
    private FileStorageService fileStorageService;
	
	@Autowired
	private GalaxyDataService galaxyDataService;   

	@Autowired
	private WorkflowService workflowService;
	
	/**
	 * Check whether the specified primaryfile exists in Amppd; if not, upload it from the resource file with a filename same as the workflow name, 
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
		}
		
		// if the primaryfile doesn't have media file uploaded, load the resource file
		if (primaryfile.getPathname()  == null) {
//			final String workflowContents;
//			try {
//				workflowContents = Resources.asCharSource(getClass().getResource(workflowName + ".ga"), Charsets.UTF_8).read();
//			} catch (IOException e) {
//				throw new RuntimeException("Failed to upload workflow " + workflowName + " into Galaxy.", e);
//			}
//			workflow = workflowService.getWorkflowsClient().importWorkflow(workflowContents);
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
			final String workflowContents;
			try {
				workflowContents = Resources.asCharSource(getClass().getResource(workflowName + ".ga"), Charsets.UTF_8).read();
			} catch (IOException e) {
				throw new RuntimeException("Failed to upload workflow " + workflowName + " from resource file into Galaxy.", e);
			}
			workflow = workflowService.getWorkflowsClient().importWorkflow(workflowContents);
		}
		
		return workflow;
	}


}
