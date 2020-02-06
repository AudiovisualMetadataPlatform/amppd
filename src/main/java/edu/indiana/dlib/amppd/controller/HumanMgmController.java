package edu.indiana.dlib.amppd.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.Invocation;
import com.github.jmchilton.blend4j.galaxy.beans.InvocationDetails;
import com.github.jmchilton.blend4j.galaxy.beans.JobInputOutput;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.BatchService;
import edu.indiana.dlib.amppd.service.BatchValidationService;
import edu.indiana.dlib.amppd.service.JobService;
import edu.indiana.dlib.amppd.web.BatchValidationResponse;
import edu.indiana.dlib.amppd.web.HmgmResourceResponse;
import lombok.extern.java.Log;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Log 
public class HumanMgmController {
	

	@Autowired
	private JobService jobService;   
	
	@GetMapping(path = "/hmgm/transcript-editor", produces = "application/json")
	public @ResponseBody HmgmResourceResponse transcriptEditor(String workflowId, Long primaryfileId, String stepId) {	
		HmgmResourceResponse response = new HmgmResourceResponse();
		response.setMediaUrl("http://test.com/test.mp4");		

    	String datasetId = null;
    	 
		List<Invocation> invocations = jobService.listJobs(workflowId, primaryfileId);
    	if(invocations.size()>0) {
    		Invocation invocation = invocations.get(0);
    		
        	InvocationDetails idetails = (InvocationDetails)jobService.getWorkflowsClient().showInvocation(workflowId, invocation.getId(), true);
    		stepId = idetails.getSteps().get(2).getId();
    		
    		Set<String> keys = idetails.getSteps().get(2).getOutputs().keySet();
    		Map<String, JobInputOutput> outputs = idetails.getSteps().get(2).getOutputs();
    		for(String key : keys){
        		datasetId = idetails.getSteps().get(2).getOutputs().get(key).getId();
    		}
    		
        	Dataset dataset = jobService.showJobStepOutput(workflowId, invocation.getId(), stepId, datasetId);
        	
        	response.setResourceUrl(dataset.getFullDownloadUrl());
        	
    	}
		
		
		return response;
	}
	public void getStepOutput(String workflowId, Long primaryfileId, String stepId) {
	}
}
