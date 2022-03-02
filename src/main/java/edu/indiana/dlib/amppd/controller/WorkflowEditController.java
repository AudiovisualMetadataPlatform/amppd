package edu.indiana.dlib.amppd.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.web.GalaxyLoginRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to proxy requests related to workflow edit between AMP UI and Galaxy workflow editor.
 * @author yingfeng
 */
@RestController
@Slf4j
public class WorkflowEditController {

	@Autowired
	private GalaxyPropertyConfig galaxyPropertyConfig;
	
//	@Autowired
	private RestTemplate restTemplate = new RestTemplate();
	private String csrfToken = null;
	private String galaxySession = null;

	/**
	 * 
	 */
//	@PostMapping("/workflows/{workflowId}")
//	public ResponseEntity<String> loginToGalaxy(@PathVariable("workflowId") String workflowId) {
	@PostMapping("/workflows/editStart")
	public ResponseEntity<String> loginGalaxy() {
		// request Galaxy server for login page with CSRF token in response
		String urlRootLogin = galaxyPropertyConfig.getBaseUrl() + "/root/login";
		ResponseEntity<String> responseRootLogin = restTemplate.getForEntity(urlRootLogin, String.class);
		galaxySession = responseRootLogin.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		
		// retrieve CSRF token from the response body using regex
		// TODO do we need to enforce utf-8
		Pattern pattern = Pattern.compile("\"session_csrf_token\": \"(.+?)\"");
	    Matcher matcher = pattern.matcher(responseRootLogin.getBody());
	    if (!matcher.find() ) {
	    	throw new GalaxyWorkflowException("Could not get CSRF token from Galaxy for login.");
	    }
    	csrfToken = matcher.group(1);

    	// send Galaxy login request with workflow editor user credentials and CSRF token
    	HttpHeaders headers = new HttpHeaders();
    	headers.add(HttpHeaders.COOKIE, galaxySession);
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	String urlRedirect = "/"; //"/workflow/editor?id=" + workflowId;
    	GalaxyLoginRequest glr = new GalaxyLoginRequest(
    			galaxyPropertyConfig.getUsernameWorkflowEdit(),
    			galaxyPropertyConfig.getPasswordWorkflowEdit(),
    			null,
    			urlRedirect,
    			csrfToken,
    			null,
    			null,
    			false,
    			false
    	);
    	String urlUserLogin = galaxyPropertyConfig.getBaseUrl() + "/user/login";
    	HttpEntity<GalaxyLoginRequest> requestUserLogin = new HttpEntity<GalaxyLoginRequest>(glr, headers);
    	ResponseEntity<String> responseUserLogin = restTemplate.postForEntity(urlUserLogin, requestUserLogin, String.class);
    	galaxySession = responseUserLogin.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    	
		return responseUserLogin;
	}	
	
	@PostMapping("/workflows/editEnd")
	public ResponseEntity<String> logoutGalaxy() {
		// request Galaxy server logout with CSRF token
		String urlUserLogout = galaxyPropertyConfig.getBaseUrl() + "/user/logout";
		urlUserLogout += "?session_csrf_token=" + csrfToken + "&logout_all=false";
    	HttpHeaders headers = new HttpHeaders();
    	headers.add(HttpHeaders.COOKIE, galaxySession);
    	HttpEntity<String> requestUserLogout = new HttpEntity<String>(null, headers);
    	ResponseEntity<String> responseUserLogout = restTemplate.exchange(urlUserLogout, HttpMethod.GET, requestUserLogout, String.class);
    	galaxySession = responseUserLogout.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    	return responseUserLogout;
	}
	
}
