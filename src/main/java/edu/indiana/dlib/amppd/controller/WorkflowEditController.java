package edu.indiana.dlib.amppd.controller;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.security.JwtTokenUtil;
import edu.indiana.dlib.amppd.web.GalaxyLoginRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller to proxy requests related to workflow edit between AMP UI and Galaxy workflow editor.
 * @author yingfeng
 */
@RestController
@Slf4j
public class WorkflowEditController {

	// Galaxy root path relative to AMP root path
	public static final String GALAXY_PATH = "/galaxy";
	
	// workflow edit cookie name
	public static final String WORKFLOW_EDIT_COOKIE = "workflowEdit";

//	// galaxySession cookie name
//	public static final String GALAXY_SESSION_COOKIE = "galaxySession";
	
	@Autowired
	private AmppdPropertyConfig amppdPropertyConfig;	
	
	@Autowired
	private GalaxyPropertyConfig galaxyPropertyConfig;
		
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private ServletContext context;
	
//	@Autowired
	private RestTemplate restTemplate = new RestTemplate();
	
	private String csrfToken = null;
	private String galaxySession = null;
	private HttpCookie galaxySessionCookie = null;

	/**
	 *  Upon initialization of the controller, 
	 *  login to galaxy as AMP workflow edit user and set up galaxy session for workflow edit.
	 */
	@PostConstruct
	private ResponseEntity<String> loginGalaxy() {
		// request Galaxy server for login page
		String urlRootLogin = galaxyPropertyConfig.getBaseUrl() + "/root/login";
		ResponseEntity<String> responseRootLogin = restTemplate.getForEntity(urlRootLogin, String.class);
		galaxySession = responseRootLogin.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
		galaxySessionCookie = HttpCookie.parse(galaxySession).get(0);
		
		// retrieve CSRF token from the response body using regex
		// TODO do we need to enforce utf-8
		Pattern pattern = Pattern.compile("\"session_csrf_token\": \"(.+?)\"");
	    Matcher matcher = pattern.matcher(responseRootLogin.getBody());
	    if (!matcher.find() ) {
	    	throw new GalaxyWorkflowException("Could not get CSRF token from Galaxy for login.");
	    }
    	csrfToken = matcher.group(1);

    	// populate login request header with previously returned galaxySession cookie
    	HttpHeaders headers = new HttpHeaders();
    	headers.add(HttpHeaders.COOKIE, galaxySession);
    	headers.setContentType(MediaType.APPLICATION_JSON);
    	
    	// populate login request payload with galaxy workflow editor super user credentials and CSRF token
    	String urlRedirect = "/"; //"/workflow/editor?id=" + workflowId;
    	GalaxyLoginRequest glr = new GalaxyLoginRequest(
    			// Below AMP WorkflowEditUser is currently not used, instead, the AMP super Galaxy user above is used for workflow edit,
    			// to avoid complexity of access control in Galaxy. We can revert to use the below user as needed in the future.
//    			galaxyPropertyConfig.getUsernameWorkflowEdit(),
//    			galaxyPropertyConfig.getPasswordWorkflowEdit(),
    			galaxyPropertyConfig.getUsername(),
    			galaxyPropertyConfig.getPassword(),
    			null,
    			urlRedirect,
    			csrfToken,
    			null,
    			null,
    			false,
    			false
    	);
    	
    	// send Galaxy login request 
    	String urlUserLogin = galaxyPropertyConfig.getBaseUrl() + "/user/login";
    	HttpEntity<GalaxyLoginRequest> requestUserLogin = new HttpEntity<GalaxyLoginRequest>(glr, headers);
    	ResponseEntity<String> responseUserLogin = restTemplate.postForEntity(urlUserLogin, requestUserLogin, String.class);
    	
    	// retrieve the galaxySession cookie and update the stored value
    	galaxySession = responseUserLogin.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    	galaxySessionCookie = HttpCookie.parse(galaxySession).get(0);
    	
    	// check response status
    	if (responseUserLogin.getStatusCode().isError() || galaxySession == null) {
    		new RuntimeException("Failed to login to Galaxy for workflow edit.");
    	}		
    	else {
    		log.info("Successfully logged in to Galaxy for workflow edit.");
    	}    	
    	return responseUserLogin;
	}
	
	/**
	 * Upon destruction of the controller,
	 * logout from galaxy as AMP workflow edit user destroy galaxy session for workflow edit.
	 */
	@PreDestroy
	private ResponseEntity<String> logoutGalaxy() {
		// TODO get logout csrf token and send logout request to galaxy

		// request Galaxy server logout with CSRF token
		String urlUserLogout = galaxyPropertyConfig.getBaseUrl() + "/user/logout";
		urlUserLogout += "?session_csrf_token=" + csrfToken + "&logout_all=false";
    	HttpHeaders headers = new HttpHeaders();
    	headers.add(HttpHeaders.COOKIE, galaxySession);
    	HttpEntity<String> requestUserLogout = new HttpEntity<String>(null, headers);
    	ResponseEntity<String> responseUserLogout = restTemplate.exchange(urlUserLogout, HttpMethod.GET, requestUserLogout, String.class);
    	
    	// retrieve the galaxySession cookie and update the stored value
    	galaxySession = responseUserLogout.getHeaders().getFirst(HttpHeaders.SET_COOKIE);	
    	
    	// check response status
    	if (responseUserLogout.getStatusCode().isError() || galaxySession == null) {
    		new RuntimeException("Failed to logout from Galaxy for workflow edit.");
    	}		
    	else {
    		log.info("Successfully logged out from Galaxy for workflow edit.");
    	}    	
		return responseUserLogout;
	}
	
	/**
	 * If galaxySession cookie doesn't exists or expired, relogin to Galaxy to get a new one. 
	 */
	private void refreshGalaxySession() {
		// TODO
	}
	
	
	/**
	 * Start a workflow edit session within an authenticated AMP user session.
	 * @param authHeader Authorization header from the request
	 * @param workflowId ID of the workflow for edit
	 * @param response HTTP response to send
	 * @return empty body response upon success
	 */
	@PostMapping("/workflows/{workflowId}/editStart")
	public ResponseEntity<String> startEdit(@RequestHeader("Authorization") String authHeader, @PathVariable("workflowId") String workflowId, HttpServletResponse response) {
		/* Note:
		 * We use AMP authorization token instead of username to identify workflow edit session, as the former corresponds to
		 * an AMP user session, and there could be multiple sessions from multiple clients for the same user.
		 * Also, with current implementation, AMP server does allow user to edit multiple workflows in the same session;
		 * however, AMP client can only take one workflow edit cookie at a time, so it should be disallowed by AMP client.
		 */

		// since the request is through AMP user authentication, the authorization token must be valid at this point
		String authToken = jwtTokenUtil.retrieveToken(authHeader);
		
		// generate a workflow edit token corresponding to the authorization token and workflow ID
		String wfeToken = jwtTokenUtil.generateWorkflowEditToken(authToken, workflowId);
		
    	// wrap the workflow edit token in a cookie 
		Cookie cookie = new Cookie(WORKFLOW_EDIT_COOKIE, wfeToken);
//	    cookie.setSecure(true);	// TODO setting secure to true doesn't work on localhost, which uses http instead of https
	    cookie.setHttpOnly(true);
	    cookie.setPath(context.getContextPath() + GALAXY_PATH);
	    cookie.setMaxAge(amppdPropertyConfig.getWorkflowEditMinutes() * 60);
		
		// send the cookie to AMP client to authenticate future workflow edit requests
		response.addCookie(cookie);
		log.info("Successfully started the edit session for workflow " + workflowId);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}	
	
	/**
	 * Start a workflow edit session within an authenticated AMP user session.
	 * @param authHeader Authorization header from the request
	 * @param workflowId ID of the workflow for edit
	 * @param response HTTP response to send
	 * @return empty body response upon success
	 */
	@PostMapping("/workflows/{workflowId}/editEnd")
	public ResponseEntity<String> endEdit(@PathVariable("workflowId") String workflowId, HttpServletResponse response) {		
    	// unset the workflow edit cookie 
		Cookie cookie = new Cookie(WORKFLOW_EDIT_COOKIE, null);
//	    cookie.setSecure(true);
	    cookie.setHttpOnly(true);
	    cookie.setPath(context.getContextPath() + GALAXY_PATH);
	    cookie.setMaxAge(0);
		
		// send the unset cookie to AMP client to delete it
		response.addCookie(cookie);
		log.info("Successfully ended the edit session for workflow " + workflowId);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}	
	
	/**
	 * Proxy all requests sent to /rest/galaxy/**, presumably for workflow edit:
	 * authenticate requests with valid workflow edit cookie, and validate requests parameters and payload;
	 * send valid request to Galaxy with valid galaxy session cookie, and return response from Galaxy. 
	 * @param method HTTP method of the workflow edit request
	 * @param wfeCookie workflow edit cookie attached to the request
	 * @param body body of the request
	 * @param request the HttpServletRequest
	 * @return response from Galaxy, including error response
	 */
	@RequestMapping(value = GALAXY_PATH + "/**")
	public ResponseEntity<byte[]> proxyEdit(
			HttpMethod method,
			@CookieValue(name = WORKFLOW_EDIT_COOKIE, required = false) String wfeCookie,
			@RequestHeader HttpHeaders headers,
			@RequestBody(required = false) byte[] body,
			HttpServletRequest request) {
	    
		// retrieve workflow edit cookie and validate it 
		ImmutablePair<AmpUser, String> pair = validateWorkflowEditCookie(wfeCookie);
		
		// respond with unauthorized status if fails
		if (pair == null) {
			log.error("Unauthorized workflow edit request: " + request.getRequestURL());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		
		// validate request URL, parameters, payload etc
		String workflowId = pair.getRight();
		if (!validateRequest(request, workflowId)) {
			log.error("Invalid workflow edit request: " + request.getRequestURL());
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
		}
		
		// replace the workflowEdit cookie with galaxySession cookie:
		// most likely Galaxy doesn't send any other cookies during workflow edit, 
		// but just in case, we need to retain other cookies;
		List<String> cookies = headers.get(HttpHeaders.COOKIE);
		List<String> gcookies = new ArrayList<String>();
		cookies.forEach((cookie) -> {
			if (isWorkflowEditCookie(cookie)) {
				gcookies.add(galaxySessionCookie.toString());
	        }
			else {
				gcookies.add(cookie);
			}
	    });		
		headers.put(HttpHeaders.COOKIE, gcookies);
		
		// remove the Origin and Referer header to avoid cors request failure 
		// due to strict-origin-when-cross-origin Referrer Policy on Galaxy side
		headers.remove(HttpHeaders.ORIGIN);
		headers.remove(HttpHeaders.REFERER);		
		
    	// forward valid request to Galaxy and return response from Galaxy
    	String url = galaxyPropertyConfig.getBaseUrl() + request.getRequestURI() + "?" + request.getQueryString();
    	HttpEntity<byte[]> grequest = new HttpEntity<byte[]>(body, headers);
    	
    	byte[] gbody;
    	HttpHeaders gheaders;
    	HttpStatus gstatus;    	
    	try {
    		ResponseEntity<byte[]> gresponse = restTemplate.exchange(url, method, grequest, byte[].class);
    		gbody = gresponse.getBody();
    		gheaders = gresponse.getHeaders();
    		gstatus = gresponse.getStatusCode();
        	log.info("Successfully processed workflow edit request " + method + " " + url + " with response status " + gstatus + " and body length " + gbody.length);
    	}
    	// in case of any Galaxy client/server error return the error response as well
    	catch (HttpStatusCodeException ex) {
    		gbody = ex.getResponseBodyAsByteArray();
    		gheaders = ex.getResponseHeaders();
    		gstatus = ex.getStatusCode();
//    		gresponse = new ResponseEntity<String>(ex.getResponseBodyAsString(), ex.getResponseHeaders(), ex.getStatusCode());
	    	log.info("Failed to process workflow edit request " + url + " with error " + gstatus);
    	}
    	
    	gheaders.forEach((key, value) -> {
			log.debug("Galaxy response header " + key + ": " + value);
	    });    	
//		log.debug("Galaxy response body length: " + gbody.length);
//		log.debug("Galaxy response body last line: " + gbody.substring(gbody.lastIndexOf("\n")));
//		log.debug("response body START: \n" + response.getBody() + "\nresponse body END");
		
		// remove CONTENT_LENGTH header as it could cause truncation of response body
		// note that we can't directly modify gheaders as it's readonly
    	HttpHeaders rheaders = new HttpHeaders();
    	rheaders.addAll(gheaders);
    	rheaders.remove(HttpHeaders.CONTENT_LENGTH);
    	ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(gbody, rheaders, gstatus);    	
		return response;
	}
	
	private boolean isWorkflowEditCookie(String cookie) {
		return cookie.startsWith(WORKFLOW_EDIT_COOKIE);
	}

//	private boolean isHeaderWorkflowEditCookie(String key, List<String> value) {
//		return key.equalsIgnoreCase(HttpHeaders.COOKIE) && value.size() == 1 && value.get(0).contains(WORKFLOW_EDIT_COOKIE);
//	}
	
	public ImmutablePair<AmpUser, String> validateWorkflowEditCookie(String wfeCookie) {
		if (StringUtils.isEmpty(wfeCookie)) {
			log.error("Workflow Edit Cookie is not provided in the request.");
			return null;
		}			
		return jwtTokenUtil.validateWorkflowEditToken(wfeCookie);
	}
	
	public boolean validateRequest(HttpServletRequest request, String workflowId) {
		// TODO
		return true;
	}
	
}
