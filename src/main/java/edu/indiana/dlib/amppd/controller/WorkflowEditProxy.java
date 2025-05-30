package edu.indiana.dlib.amppd.controller;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.exception.GalaxyWorkflowException;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.security.JwtTokenUtil;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.web.GalaxyLoginRequest;
import edu.indiana.dlib.amppd.web.GalaxyUpdateWorkflowRequest;
import edu.indiana.dlib.amppd.web.GalaxyWorkflowRequest;
import edu.indiana.dlib.amppd.web.GalaxyWorkflowResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Proxy between AMP client and AMP servr for requests related to workflow edit between AMP UI and Galaxy workflow editor.
 * @author yingfeng
 */
@RestController
@Slf4j
public class WorkflowEditProxy {

//	// galaxySession cookie name
//	public static final String GALAXY_SESSION_COOKIE = "galaxySession";

	// Galaxy root path relative to AMP context path
	public static final String GALAXY_ROOT = "/galaxy";
	
	// Galaxy workflow editor generic request paths relative to Galaxy root
	private static final List<String> GALAXY_PATHS = Arrays.asList("/favicon.ico");

	// Galaxy static path relative to galaxy root path
	public static final String GALAXY_STATIC = "/static";
			
	// Galaxy API path relative to galaxy root path
	public static final String GALAXY_API = "/api";
	
	// Galaxy workflow editor API request paths relative to Galaxy API path
	private static final List<String> GALAXY_API_PATHS = Arrays.asList("/webhooks", "/licenses", "/datatypes/types_and_mapping");
	
	// workflow edit cookie name
	public static final String WORKFLOW_EDIT_COOKIE = "workflowEdit";
	
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
	
	@Autowired
	private AmpUserService ampUserService;

	@Autowired
	private PermissionService permissionService;

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
		log.info(galaxySessionCookie.getValue());
		log.info(galaxySession);
		
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
	 * Create a new workflow for edit with default info within an authenticated AMP user session.
	 * @param authHeader Authorization header from the request
	 * @return ID of the newly created workflow
	 */
	@PostMapping("/workflows/create")
	public ResponseEntity<String> create(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
		// check permission 
		// Note: Since workflow is not associated with any unit, the AC is checked against any unit
		boolean can = permissionService.hasPermission(ActionType.Create, TargetType.Workflow, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot create workflow in any unit.");
		}
		
		String workflowId = null;
		
		// create workflow in Galaxy
		try {
			// populate Galaxy request header with galaxySession cookie
	    	HttpHeaders headers = new HttpHeaders();
	    	headers.add(HttpHeaders.COOKIE, galaxySession);
	    	headers.setContentType(MediaType.APPLICATION_JSON);
	
	    	// populate Galaxy request body with default GalaxyWorkflowRequest, i.e. with default name and empty annotation
	    	GalaxyWorkflowRequest gwreq = new GalaxyWorkflowRequest();
			// send PUT request with headers and body to Galaxy 
			String url = galaxyPropertyConfig.getBaseUrl() + "/workflow/create";
			HttpEntity<GalaxyWorkflowRequest> request = new HttpEntity<GalaxyWorkflowRequest>(gwreq, headers);
			ResponseEntity<GalaxyWorkflowResponse> response = restTemplate.exchange(url, HttpMethod.PUT, request, GalaxyWorkflowResponse.class);
			GalaxyWorkflowResponse gwres = response.getBody();
			
			// check Galaxy response status
			if (response.getStatusCode() != HttpStatus.OK) {
				throw new GalaxyWorkflowException("Error while creating workflow in Galaxy: " + gwres.getMessage());
			}
			
			// retrieve workflowId from galaxy response
			workflowId = gwres.getId();
			if (StringUtils.isEmpty(workflowId)) {
				throw new GalaxyWorkflowException("Failed to create workflow in Galaxy: empty workflow ID returned.");
			}
			try {
				String update_url = galaxyPropertyConfig.getBaseUrl() + "/api/workflows/" + workflowId.toString();
				GalaxyUpdateWorkflowRequest updateRequest = new GalaxyUpdateWorkflowRequest(ampUserService.getCurrentUsername(), gwreq.getWorkflow_name(), gwreq.getWorkflow_annotation());
				String workflow_creator = updateRequest.params();
				HttpEntity<String> update_request = new HttpEntity<String>(workflow_creator, headers);
				restTemplate.exchange(update_url, HttpMethod.PUT, update_request, GalaxyWorkflowResponse.class);
			}catch(RestClientException e) {
				throw new GalaxyWorkflowException("Exception while updating workflow creator in Galaxy", e);
			}
		}
		catch(RestClientException e) {
			throw new GalaxyWorkflowException("Exception while creating workflow in Galaxy", e);
		}
		
		// generate the workflow edit cookie, provided the current AMP request authHeader and workflowId
		ResponseCookie rc = generateWorkflowEditCookie(authHeader, workflowId);

		// send the cookie to AMP client to authenticate future workflow edit requests
		ResponseEntity<String> res = ResponseEntity.status(HttpStatus.OK).header(HttpHeaders.SET_COOKIE, rc.toString()).body(workflowId);
	    	    
		log.info("Successfully created new workflow for edit: " + workflowId);
		return res;
	}	
	
	/**
	 * Start a workflow edit session within an authenticated AMP user session.
	 * @param authHeader Authorization header from the request
	 * @param workflowId ID of the workflow for edit
	 * @param response HTTP response to send back
	 * @return empty body response upon success
	 */
	@PostMapping("/workflows/{workflowId}/editStart")
	public ResponseEntity<String> startEdit(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader, @PathVariable String workflowId, HttpServletResponse response) {
		// check permission 
		// Note: Since workflow is not associated with any unit, the AC is checked against any unit
		boolean can = permissionService.hasPermission(ActionType.Update, TargetType.Workflow, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update workflow in any unit.");
		}
		
		// generate the workflow edit cookie, provided the current AMP request authHeader and workflowId
		ResponseCookie rc = generateWorkflowEditCookie(authHeader, workflowId);
		
		// send the cookie to AMP client to authenticate future workflow edit requests
		// note that the response body is empty
	    response.setHeader(HttpHeaders.SET_COOKIE, rc.toString());
	    
		log.info("Successfully started the edit session for workflow " + workflowId);
		return ResponseEntity.status(HttpStatus.OK).body(null);
	}	
	
	/**
	 * Start a workflow edit session within an authenticated AMP user session.
	 * @param authHeader Authorization header from the request
	 * @param workflowId ID of the workflow for edit
	 * @param response HTTP response to send back
	 * @return empty body response upon success
	 */
	@PostMapping("/workflows/{workflowId}/editEnd")
	public ResponseEntity<String> endEdit(@PathVariable("workflowId") String workflowId, HttpServletResponse response) {		
		// check permission 
		// Note: Since workflow is not associated with any unit, the AC is checked against any unit
		boolean can = permissionService.hasPermission(ActionType.Update, TargetType.Workflow, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update workflow in any unit.");
		}
		
    	// unset the workflow edit cookie 
		Cookie cookie = new Cookie(WORKFLOW_EDIT_COOKIE, null);
	    cookie.setSecure(true);
	    cookie.setHttpOnly(true);
	    cookie.setPath(context.getContextPath() + GALAXY_ROOT);
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
	/* Note: 
	 * The hardcoded origins URL below is a temporary work-around to allow AMP UI devs to connect to AMP Test server 
	 * from their localhost client for dev work. This doesn't impact other use cases.  
	 */	
	@CrossOrigin(origins = "https://amp-test.dlib.indiana.edu", allowedHeaders = "*", exposedHeaders = "*", allowCredentials = "true" )
	@RequestMapping(value = GALAXY_ROOT + "/**")
	public ResponseEntity<byte[]> proxyEdit(
			HttpMethod method,
			@CookieValue(name = WORKFLOW_EDIT_COOKIE, required = false) String wfeCookie,
			@RequestHeader HttpHeaders headers,
			@RequestBody(required = false) byte[] body,
			HttpServletRequest request) {
		// check permission 
		// Note: Since workflow is not associated with any unit, the AC is checked against any unit
		boolean can = permissionService.hasPermission(ActionType.Update, TargetType.Workflow, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot update workflow in any unit.");
		}
		
	    log.debug("Proxying workflow edit request " + method + " " + request.getRequestURL() + "...");
	    
		// retrieve workflow edit cookie and validate it 
		ImmutablePair<AmpUser, String> pair = validateWorkflowEditCookie(wfeCookie);
		
		// respond with unauthorized status if fails
		if (pair == null) {
			log.error("Unauthorized workflow edit request: " + method + " " + request.getRequestURL());
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}
		
		// filter request on its URL, parameters, payload etc
		String workflowId = pair.getRight();
//		if (!filterRequest(request, body, workflowId)) {
		if (!filterRequest(request, workflowId)) {
			log.error("Invalid workflow edit request: " + method + " " + request.getRequestURL());
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
		
//		// remove the Origin and Referer header to avoid cors request failure 
//		// due to strict-origin-when-cross-origin Referrer Policy on Galaxy side
//		headers.remove(HttpHeaders.ORIGIN);
//		headers.remove(HttpHeaders.REFERER);
		
		log.debug("Galaxy request header " + headers);			
//    	headers.forEach((key, value) -> {
//			log.debug("Galaxy request header " + headers);
//	    });    	
		
		// set up request to Galaxy
		String query = StringUtils.isEmpty(request.getQueryString()) ? "" : "?" + request.getQueryString();
    	String url = galaxyPropertyConfig.getBaseUrl() + request.getRequestURI() + query;
    	HttpEntity<byte[]> grequest = new HttpEntity<byte[]>(body, headers);    	
    	byte[] gbody;
    	HttpHeaders gheaders;
    	HttpStatus gstatus;    
    	    	
    	// forward request to Galaxy and receive response from Galaxy
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
	    	log.error("Failed to process workflow edit request " + method + " " + url + " with error " + gstatus);
    	}
    	
//    	gheaders.forEach((key, value) -> {
//			log.debug("Galaxy response header " + key + ": " + value);
//	    });    	
//		log.debug("Galaxy response body length: " + gbody.length);
//		log.debug("Galaxy response body last line: " + gbody.substring(gbody.lastIndexOf("\n")));
//		log.debug("response body START: \n" + response.getBody() + "\nresponse body END");
		log.debug("Galaxy response header " + gheaders);	
		
		// remove CONTENT_LENGTH header as it could cause truncation of response body
		// note that we can't directly modify gheaders as it's readonly
    	HttpHeaders rheaders = new HttpHeaders();
    	rheaders.addAll(gheaders);
    	rheaders.remove(HttpHeaders.CONTENT_LENGTH);

    	// return workflow edit response
    	ResponseEntity<byte[]> response = new ResponseEntity<byte[]>(gbody, rheaders, gstatus);    	
		return response;
	}	
	
	/**
	 * Generates the workflow edit cookie for the the given AMP request authHeader and workflowId.
	 */
	private ResponseCookie generateWorkflowEditCookie(String authHeader, String workflowId) {
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
		
		/* Note:
		 * Setting secure=false allows WF editor to work with VM when accessing UI from a browser outside of VM,  i.e. 
		 * with http on hosts other than localhost. Meanwhile, Setting sameSite=Strict blocks cross-site access for security.
		 */
    	// wrap the workflow edit token in a cookie 
		ResponseCookie rc = ResponseCookie.from(WORKFLOW_EDIT_COOKIE, wfeToken) // key & value
		        .httpOnly(true)
		        .secure(false)	
		        .sameSite("Strict")  
		        .path(context.getContextPath() + GALAXY_ROOT)
		        .maxAge(amppdPropertyConfig.getWorkflowEditMinutes() * 60)
		        .build();
		        
//		Cookie cookie = new Cookie(WORKFLOW_EDIT_COOKIE, wfeToken);
//	    cookie.setSecure(true);	// TODO setting secure to true doesn't work on localhost, which uses http instead of https
//	    cookie.setHttpOnly(true);
//	    cookie.setPath(context.getContextPath() + GALAXY_ROOT);
//	    cookie.setMaxAge(amppdPropertyConfig.getWorkflowEditMinutes() * 60);
//		response.addCookie(cookie);

		return rc;
	}
	
	/**
	 * Return true if the given HTTP request is a legitimate one initiated during the workflow edit session
	 * for the given workflow; false otherwise.
	 */
//	private boolean filterRequest(HttpServletRequest request, byte[] body, String workflowId) {
	private boolean filterRequest(HttpServletRequest request, String workflowId) {
		String method = request.getMethod();		
		String path = StringUtils.substringAfter(request.getServletPath(), GALAXY_ROOT);
//		String payload = new String(body, StandardCharsets.UTF_8);
				
		// filter POST requests:
		// the only POST request occurs when adding a tool to the workflow,
		// and the payload contains the tool JSON
		if (method.equals(HttpMethod.POST.toString())) {
			// URL path must be /api/workflows/build_module
			if (!path.equals(GALAXY_API + "/workflows/build_module")) {
				log.error("Invalid path " + path + " for POST request to add an MGM to the workflow.");
				return false;
			}
			// payload must be valid tool JSON: this will be handled by Galaxy
			return true;
		}
		
		// filter PUT requests:
		// the only PUT request occurs when saving the workflow,
		// and the payload contains the workflow  JSON
		if (method.equals(HttpMethod.PUT.toString())) {
			// URL path must be /api/workflows/workflowId
			if (!path.startsWith(GALAXY_API + "/workflows/")) {
				log.error("Invalid path " + path + " for PUT request to save the workflow.");
				return false;
			}
			// workflow ID on the request path must match the ID of the workflow currently being edited
			return checkWorkflowId(path, "/workflows/", null, workflowId, "PUT", "save");
			// payload must be valid tool info: this will be handled by Galaxy
		}		
		
		// filter GET requests
		if (method.equals(HttpMethod.GET.toString())) {
			// filter the request initiated by the workflow edit session to load the editor
			if (path.equals("/workflow/editor")) {
				// workflow ID in the request parameter must match the ID of the workflow currently being edited
				return checkWorkflowId(request, workflowId, "load the editor for");
			}
			
			// filter GET request for loading the workflow
			if (path.equals("/workflow/load_workflow")) {
				// workflow ID in the request parameter must match the ID of the workflow currently being edited
				return checkWorkflowId(request, workflowId, "load");
			}
			
			// filter GET request for retrieving the workflow versions
			if (path.startsWith(GALAXY_API + "/workflows/") && path.endsWith("/versions")) {
				// workflow ID on the request path must match the ID of the workflow currently being edited
				return checkWorkflowId(path, "/workflows/", "/versions", workflowId, "GET", "fetch the versios of");
			}

			// filter GET requests on static info:
			// various requests for static info are triggered during workflow loading and saving;
			// since Galaxy client could change between releases, it's more flexible and robust 
			// not to assume specific URLs, but allow a more generic URL patterns instead;
			// for now, all GET static requests are allowed as they are public info 
			if (path.startsWith(GALAXY_STATIC)) {
				return true;
			}
			
			// filter GET API requests during workflow loading/saving 
			// check against the list of all allowed API requests (other than the workflow versions)
			if (path.startsWith(GALAXY_API)) {
				String apipath = StringUtils.substringAfter(path, GALAXY_API);
				if (GALAXY_API_PATHS.contains(apipath)) {
					return true;
				}
			}

			// filter other GET requests during workflow loading/saving 			
			if (GALAXY_PATHS.contains(path)) {
				return true;
			}		
		}
		
		// all other requests are invalid 
		return false;
	}
	
	/**
	 * Returns true if the give cookie is a WFE cookie; false otherwise
	 */
	private boolean isWorkflowEditCookie(String cookie) {
		return cookie.startsWith(WORKFLOW_EDIT_COOKIE);
	}

//	private boolean isHeaderWorkflowEditCookie(String key, List<String> value) {
//		return key.equalsIgnoreCase(HttpHeaders.COOKIE) && value.size() == 1 && value.get(0).contains(WORKFLOW_EDIT_COOKIE);
//	}
	
	/**
	 * Return true if the given WFE cookie contains valid JWT token for AMP user workflow edit session; false otherwise.
	 */
	private ImmutablePair<AmpUser, String> validateWorkflowEditCookie(String wfeCookie) {
		if (StringUtils.isEmpty(wfeCookie)) {
			log.error("Workflow Edit Cookie is not provided in the request.");
			return null;
		}			
		return jwtTokenUtil.validateWorkflowEditToken(wfeCookie);
	}
		
	/**
	 * Return true if the workflow ID parameter in the given GET request for the given action matches the given workflow ID; 
	 * false otherwise.
	 */
	private boolean checkWorkflowId(HttpServletRequest request, String workflowId, String action) {
		String wfid = request.getParameter("id");
		if (StringUtils.equals(wfid,  workflowId)) {
			return true;
		}
		log.error("Invalid workflow ID parameter " + wfid + " in GET request to " + action + " the workflow " + workflowId);
		return false;
	}

	/**
	 * Retrieve the workflow ID between the given start/end string in the given request path of the given method
	 * for the given action, return true if it matches the given workflow ID; false otherwise.
	 */
	private boolean checkWorkflowId(String path, String start, String end, String workflowId, String method, String action) {
		String wfid = end == null ? StringUtils.substringAfter(path, start) : StringUtils.substringBetween(path, start, end);
		if (StringUtils.equals(wfid,  workflowId)) {
			return true;
		}
		log.error("Invalid workflow ID " + wfid + " in the path for " + method + " request to " + action + " the workflow " + workflowId);
		return false;	
	}
		
}
