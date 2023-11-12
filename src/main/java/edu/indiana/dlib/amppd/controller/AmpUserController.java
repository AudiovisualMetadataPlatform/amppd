package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.AmpUser.Status;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import edu.indiana.dlib.amppd.model.projection.AmpUserBrief;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.security.JwtRequest;
import edu.indiana.dlib.amppd.service.PermissionService;
import edu.indiana.dlib.amppd.service.impl.AmpUserServiceImpl;
import edu.indiana.dlib.amppd.web.AuthRequest;
import edu.indiana.dlib.amppd.web.AuthResponse;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller for REST operations on Login.
 * 
 * @author vinitab yingfeng
 *
 */
//  @CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@Slf4j
public class AmpUserController {

	@Autowired
	private AmpUserServiceImpl ampUserService;

	@Autowired
	private AmpUserRepository ampUserRepository;

	@Autowired
	private PermissionService permissionService;


	// Note:
	// Currently AMP user accounts are created via self registration, and the involved APIs which involves
	// user initiated actions are available to every one, i.e. no authorization required.
	// The APIs requiring authorization are those involving approving accounts and querying users.

	@PostMapping(path = "/account/authenticate")
	public ResponseEntity<?> authenticate(@RequestBody JwtRequest authenticationRequest) throws Exception {
		String username = authenticationRequest.getUsername();
		log.info("Authenticating login for user: " + username);
		AuthResponse response = ampUserService.authenticate(username, authenticationRequest.getPassword());

		// if authentication failed, respond with status 401
		if(!response.isSuccess()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}	

		// otherwise respond with user info and access token in status 200
		return ResponseEntity.ok(response);
	}

	@PostMapping(path = "/account/validate")
	public ResponseEntity<?> validateToken() throws Exception {
		// TODO
		// this API simply returns 200 status if the token in the request is valid,
		// the only purpose is for frontend to verify if its locally stored auth token is valid (not compromised)
		// before forwarding any route. there ise better way to achieve this without an extra API call
		return ResponseEntity.ok("Success");
	}

	@PostMapping(path = "/account/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse register(@RequestBody AmpUser user) { 
		log.info("Registering user account: " + user.getUsername());	
		AuthResponse res = ampUserService.registerAmpUser(user);
		return res;
	}

	@PostMapping(path = "/account/approve", produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse approveAccount(@RequestParam Long userId, @RequestParam Boolean approve) { 
		// check permission
		boolean can = permissionService.hasPermission(ActionType.Update, TargetType.AmpUser, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot approve/reject user account registration.");
		}		

		String action = approve ? "approve" : "reject";
		log.info(action + "ing user account: " + userId);	
		AuthResponse res = ampUserService.approveAccount(userId, approve);
		return res;
	}

	@PostMapping(path = "/account/activate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse activateUser(@RequestBody AuthRequest request) {
		log.info("Activating user account with token: " + request.getToken());	
		AuthResponse res = ampUserService.activateAccount(request.getToken());
		return res;
	}

	@PostMapping(path = "/account/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse forgotPassword(@RequestBody AuthRequest request) { 
		log.info("Sending reset-password email to user: " + request.getEmailid());	
		AuthResponse res = ampUserService.emailResetPasswordToken(request.getEmailid());
		return res;
	}

	// TODO 
	// This should be GET, or it should be combined with resetPassword API
	@PostMapping(path = "/account/reset-password-getEmail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse resetPasswordGetEmail(@RequestBody AuthRequest request) {
		log.info("Retrieving email for reset-password token:" + request.getToken());
		AuthResponse res = ampUserService.resetPasswordGetEmail(request.getToken());
		return res;
	}

	@PostMapping(path = "/account/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse resetPassword(@RequestBody AuthRequest request) { 
		log.info("Resetting password for user:" + request.getEmailid());	
		AuthResponse res = ampUserService.resetPassword(request.getEmailid(), request.getPassword(), request.getToken());
		return res;
	}

	@GetMapping(path="/users/{Id}")
	public @ResponseBody AmpUser getUser(@PathVariable Long Id) {
		// check permission
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.AmpUser, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot view details of other users.");
		}

		AmpUser ampuser = ampUserService.getUserById(Id);
		log.info("Successfully retrieved user with ID " + ampuser.getId());
		return ampuser;
	}

	/**
	 * Get the list of users with active accounts, names starting with the given pattern, but IDs not in the given list (if provided).
	 * @param nameStarting type-ahead pattern for user name
	 * @param idsExcluding IDs of the users to exclude
	 * @return the list of users satisfying the query criteria
	 */
	@GetMapping("/users/active")
	public List<AmpUserBrief> findActiveUsersByNameStartingIdsExcluding(@RequestParam String nameStarting, @RequestParam(required = false) List<Long> idsExcluding) {
		// check permission
		boolean can = permissionService.hasPermission(ActionType.Read, TargetType.AmpUser, null);
		if (!can) {
			throw new AccessDeniedException("The current user cannot view details of other users.");
		}		

		List<AmpUserBrief> users = null;

		// if users to exclude is not provided, skip the exclude-user criteria 
		if (idsExcluding == null || idsExcluding.isEmpty()) {
			users = ampUserRepository.findByStatusAndNameStartsOrderByName(Status.ACTIVATED, nameStarting);
			log.info("Successfully found " + users.size() + " active users with name starting with " + nameStarting);
		}
		else {
			users = ampUserRepository.findByStatusAndNameStartsAndIdNotInOrderByName(Status.ACTIVATED, nameStarting, idsExcluding);
			log.info("Successfully found " + users.size() + " active users with name starting with " + nameStarting + ", excluding the " + idsExcluding.size() + " users in the given list.");
		}

		return users;
	}


}

