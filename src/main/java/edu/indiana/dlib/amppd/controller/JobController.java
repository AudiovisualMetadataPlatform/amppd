package edu.indiana.dlib.amppd.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.jmchilton.blend4j.galaxy.beans.GalaxyObject;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.FileStorageService;
import edu.indiana.dlib.amppd.service.GalaxyDataService;
import edu.indiana.dlib.amppd.service.JobService;
import lombok.extern.java.Log;

/**
 * Controller for REST operations on Workflow.
 * @author yingfeng
 *
 */
@RestController
@Log
public class JobController {
	
//	@Autowired
//	private GalaxyApiService galaxyApiService;

	@Autowired
    private PrimaryfileRepository primaryfileRepository;

	@Autowired
	private GalaxyDataService galaxyDataService;

	@Autowired
	private JobService jobService;

	@Autowired
    private FileStorageService fileStorageService;
	
	/**
	 * Retrieve all workflows from Galaxy through its REST API.
	 * @return
	 */
	@GetMapping("/workflows")
	public List<Workflow> getWorkflows() {	
		List<Workflow> workflows = null;
	
		try {
//			workflows = galaxyApiService.getGalaxyInstance().getWorkflowsClient().getWorkflows();
			workflows = jobService.getWorkflowsClient().getWorkflows();
			log.info("Retrieved " + workflows.size() + " current workflows in Galaxy: " + workflows);
		}
		catch (Exception e) {
			String msg = "Unable to retrieve workflows from Galaxy instance.";
			log.severe(msg);
			throw new RuntimeException(msg, e);
		}
		
		return workflows;
	}
	
	// TODO To be more consistent with REST best practice, the following method should probably be moved to JobController, since this is essentially creating a new job. 	
	/**
	 * Run the given workflow against the given primaryfile.
	 * @return outputs of the job run
	 */
	@PostMapping("/workflows/{workflowId}/run")
	public WorkflowOutputs runWorkflow(@PathVariable("workflowId") String workflowId, 
			@RequestParam("primaryfileId") Long primaryfileId, 
			@RequestParam("parameters") Map<String, Map<String, String>> parameters) {
		WorkflowOutputs woutputs = null;
		
    	// at this point the primaryfile shall have been created and its media file uploaded into Amppd file system
    	Primaryfile primaryfile = primaryfileRepository.findById(primaryfileId).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfileId + "> does not exist!"));    
    	if (primaryfile.getPathname() == null || primaryfile.getPathname().isEmpty()) {
    		throw new StorageException("Primaryfile " + primaryfileId + " hasn't been uploaded to AMPPD file system");
    	}
    	
    	/* Note: 
    	 * We do a lazy upload from Amppd to Galaxy, i.e. we do it when workflow is invoked in Galaxy, rather than when the file is uploaded to Amppd.
    	 * The pros is that we won't upload to Galaxy unnecessarily if the primaryfile is never going to be processed through workflow;
    	 * the cons is that it might slow down workflow execution when running in batch.
    	 */

    	// upload the primaryfile into Galaxy data library, the returned result is an GalaxyObject containing the ID and URL of the dataset uploaded
    	String pathname = fileStorageService.absolutePathName(primaryfile.getPathname());
    	GalaxyObject go = galaxyDataService.uploadFileToGalaxy(pathname);		
		
    	// invoke the workflow 
    	try {
    		WorkflowInputs winputs = jobService.buildWorkflowInputs(workflowId, go.getId(), parameters);
    		return jobService.getWorkflowsClient().runWorkflow(winputs);
    	}
    	catch (Exception e) {
    		String msg = "Error running workflow " + workflowId + " on primaryfile " + primaryfileId;
    		log.severe(msg);
    		throw new GalaxyWorkflowException(msg, e);
    	}
	}

}
