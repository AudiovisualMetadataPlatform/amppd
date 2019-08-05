package edu.indiana.dlib.amppd.service.impl;

import org.apache.http.HttpHost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.model.galaxy.GalaxyApiKey;
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
	public String getApiKey() {
		RestTemplate authRestTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactoryBasicAuth(new HttpHost(config.getHost(), config.getPort(), "http")));
		authRestTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(config.getUsername(), config.getPassword()));
		String url = config.getApiUrl() + "/authenticate/baseauth";
		GalaxyApiKey key = authRestTemplate.getForObject(url, GalaxyApiKey.class);		
//		String key = "9d3ffcc9c26a6a290810a6501872ff82";
		
		/* TODO get api key with following 
		request: curl –user zipzap@foo.com:password http://localhost:8080/api/authenticate/baseauth
		response: {“api_key”: “baa4d6e3a156d3033f05736255f195f9” }
		 */
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
