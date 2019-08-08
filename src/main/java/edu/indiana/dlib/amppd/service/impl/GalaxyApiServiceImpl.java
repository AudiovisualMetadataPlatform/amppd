package edu.indiana.dlib.amppd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.model.galaxy.GalaxyUser;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import lombok.extern.java.Log;

/**
 * Implementation of Galaxy service.
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
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getInstance()
	 */
	public GalaxyInstance getInstance() {
		GalaxyUser user = getCurrentUser();
		
		// if the galaxy instance has already been retrieved and stored in the current user, no need to retrieve again 
		if (user.getInstance() != null) {
			return user.getInstance();
		}		
		
		// otherwise create a new Galaxy instance using user's credentials and store it for the current user
		GalaxyInstance instance = GalaxyInstanceFactory.getFromCredentials(config.getBaseUrl(), config.getUsername(), config.getPassword());
		user.setInstance(instance);
		return instance;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getApiKey()
	 */
	public String getApiKey() {
		// if the API key has already been retrieved and stored in the current user, no need to retrieve again 
		GalaxyUser user = getCurrentUser();
		if (user.getApiKey() != null) {
			return user.getApiKey();
		}
		
		// otherwise create a new Galaxy instance using user's credentials and store it for the current user
		String key = getInstance().getApiKey();
		user.setApiKey(key);
		return key;

//		/* Galaxy api key can be obtained via a GET request with baseauth, for ex.
//		request: curl –user zipzap@foo.com:password http://localhost:8080/api/authenticate/baseauth
//		response: {“api_key”: “baa4d6e3a156d3033f05736255f195f9” }
//		 */
//		RestTemplate authRestTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactoryBasicAuth(new HttpHost(config.getHost(), config.getPort(), "http")));
//		authRestTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(user.getUsername(), user.getPassword()));
//		String url = config.getApiUrl() + "/authenticate/baseauth";
//		GalaxyApiKey key = authRestTemplate.getForObject(url, GalaxyApiKey.class);			
//		user.setApiKey(key.getApi_key());
//		return key.getApi_key();
	}
		
//    /**
//     * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getWorkflowUrl()
//     */
//    public String getWorkflowUrl() {
//    	return config.getApiUrl() + "/workflows";
//    }
//
//	/**
//	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getWorkflows()
//	 */
//	@GetMapping("/workflows")
//	public GalaxyWorkflow[] getWorkflows() {		
//		String url = getWorkflowUrl() + "?key=" + getApiKey();
//		GalaxyWorkflow[] workflows = restTemplate.getForObject(url, GalaxyWorkflow[].class);
//		log.info("Current workflows in Galaxy: " + workflows);
//		return workflows;
//	}

}
