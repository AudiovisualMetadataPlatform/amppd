package edu.indiana.dlib.amppd.service.impl;

import java.util.List;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestTemplate;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory;
import com.github.jmchilton.blend4j.galaxy.LibrariesClient;
import com.github.jmchilton.blend4j.galaxy.beans.FilesystemPathsLibraryUpload;
import com.github.jmchilton.blend4j.galaxy.beans.Library;
import com.github.jmchilton.blend4j.galaxy.beans.LibraryContent;
import com.sun.jersey.api.client.ClientResponse;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.model.galaxy.GalaxyApiKey;
import edu.indiana.dlib.amppd.model.galaxy.GalaxyUser;
import edu.indiana.dlib.amppd.model.galaxy.GalaxyWorkflow;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import edu.indiana.dlib.amppd.web.HttpComponentsClientHttpRequestFactoryBasicAuth;
import lombok.extern.java.Log;

/**
 * Implementation of service to access Galaxy REST API.
 * @author yingfeng
 *
 */
// TODO change commented code to use galaxyRestTemplate as autowired
//@ContextConfiguration(classes = GalaxyRestTemplateFactoryConfig.class)
@Service
@Log
public class GalaxyApiServiceImpl implements GalaxyApiService {

	@Autowired
	private GalaxyPropertyConfig config;

//	@Autowired
	private RestTemplate restTemplate = new RestTemplate();
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getApiKey()
	 */
	public GalaxyUser getCurrentUser() {
		/* TODO
		 * Below is a stub for the real implementation: for now we will return a new instance of the amppd master user as the current user;
		 * once we set up user management and access control in AMP, we shall retrieve this info from the current user session.  
		 */
		GalaxyUser user = new GalaxyUser();
		user.setUsername(config.getUsername());
		user.setPassword(config.getPassword());
		return user;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getApiKey()
	 */
	public String getApiKey() {
		// the current user info is stored in the current session, supposedly we only need to retrieve the API key once and it will be stored in the current user afterwards
		GalaxyUser user = getCurrentUser();
		if (user.getApiKey() != null) {
			return user.getApiKey();
		}
		


		/* Galaxy api key can be obtained via a GET request with baseauth, for ex.
		request: curl –user zipzap@foo.com:password http://localhost:8080/api/authenticate/baseauth
		response: {“api_key”: “baa4d6e3a156d3033f05736255f195f9” }
		 */
		RestTemplate authRestTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactoryBasicAuth(new HttpHost(config.getHost(), config.getPort(), "http")));
		authRestTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(user.getUsername(), user.getPassword()));
		String url = config.getApiUrl() + "/authenticate/baseauth";
		GalaxyApiKey key = authRestTemplate.getForObject(url, GalaxyApiKey.class);			
		user.setApiKey(key.getApi_key());
		return key.getApi_key();
	}
		
    /**
     * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getWorkflowUrl()
     */
    public String getWorkflowUrl() {
    	return config.getApiUrl() + "/workflows";
    }

	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getWorkflows()
	 */
	@GetMapping("/workflows")
	public GalaxyWorkflow[] getWorkflows() {		
		String url = getWorkflowUrl() + "?key=" + getApiKey();
		GalaxyWorkflow[] workflows = restTemplate.getForObject(url, GalaxyWorkflow[].class);
		log.info("Current workflows in Galaxy: " + workflows);
		return workflows;
	}

}
