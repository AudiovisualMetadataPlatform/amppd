package edu.indiana.dlib.amppd.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.AmpUser.Status;
import edu.indiana.dlib.amppd.model.dto.AmpUserDto;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.security.JwtRequest;
import edu.indiana.dlib.amppd.security.JwtResponse;
import edu.indiana.dlib.amppd.security.JwtTokenUtil;
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
	private JwtTokenUtil jwtTokenUtil;

	@RequestMapping(value = "/account/authenticate", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		String username = authenticationRequest.getUsername();
		AuthResponse response = ampUserService.authenticate(username, authenticationRequest.getPassword());

		// authentication failed, respond with status 401
		if(!response.isSuccess()) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
		}	

		// otherwise generate JWT token and respond with status 200
		final String token = jwtTokenUtil.generateToken(username);
		return ResponseEntity.ok(new JwtResponse(token));
	}

	@RequestMapping(value = "/account/validate", method = RequestMethod.POST)
	public ResponseEntity<?> validateToken() throws Exception {
		return ResponseEntity.ok("Success");
	}

	@PostMapping(path = "/account/register", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse register(@RequestBody AmpUser user) { 
		log.info("Registration for User=> Name:"+ user.getUsername());	
		log.info("Registration for User=> Email:"+ user.getEmail());	
		AuthResponse res = ampUserService.registerAmpUser(user);
		log.info(" Registration result: " + res);
		return res;
	}

	@PostMapping(path = "/account/forgot-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse forgotPassword(@RequestBody AuthRequest request) { 
		log.info("Forgot Password for User=> Email:"+ request.getEmailid());	
		AuthResponse res = ampUserService.emailResetPasswordToken(request.getEmailid());
		log.info(" Forgot Password result: " + res);
		return res;
	}

	@PostMapping(path = "/account/reset-password", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse resetPassword(@RequestBody AuthRequest request) { 
		log.info("Reset Password for User=> Email:"+ request.getEmailid());	
		AuthResponse res = ampUserService.resetPassword(request.getEmailid(), request.getPassword(), request.getToken());
		log.info(" Reset Password result: " + res);
		return res;
	}

	@PostMapping(path = "/account/approve", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse approveUser(@RequestBody AuthRequest request) { 
		log.info("Approve User=> id:"+ request.getUserId());	
		AuthResponse res = ampUserService.accountAction(request.getUserId(), "approve");
		log.info(" approve user result: " + res);
		return res;
	}

	@PostMapping(path = "/account/reject", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse rejectUser(@RequestBody AuthRequest request) { 
		log.info("Reject User=> id:"+ request.getUserId());	
		AuthResponse res = ampUserService.accountAction(request.getUserId(), "reject");
		log.info(" reject user result: " + res);
		return res;
	}

	@PostMapping(path = "/account/activate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse activateUser(@RequestBody AuthRequest request) {
		log.info("Activate User");	
		AuthResponse res = ampUserService.activateAccount(request.getToken());
		log.info(" activate user result: " + res);
		return res;
	}

	@PostMapping(path = "/account/reset-password-getEmail", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody AuthResponse resetPasswordGetEmail(@RequestBody AuthRequest request) {
		log.info("Calling get email for a token using resetPasswordGetEmail()");	
		AuthResponse res = ampUserService.resetPasswordGetEmail(request.getToken());
		log.info(" Fetched Email id for a token using resetPasswordGetEmail():"+res.getEmailid());
		return res;
	}

	@GetMapping(path="/account/{Id}")
	public @ResponseBody AmpUser getUser(@PathVariable Long Id) {
		log.info("User=> id:"+ Id);
		AmpUser ampuser= ampUserService.getUserById(Id);
		log.info("Fetched User for given Id using getUserById()"+ampuser.getId());
		return ampuser;
	}

	/**
	 * Get the list of users with active accounts, names starting with the given pattern, but IDs not in the given list (if provided).
	 * @param nameStarting type-ahead pattern for user name
	 * @param idsExcluding IDs of the users to exclude
	 * @return the list of users satisfying the query criteria
	 */
	@GetMapping("/users/active")
	public List<AmpUserDto> findActiveUsersByNameStartingIdsExcluding(@RequestParam String nameStarting, @RequestParam(required = false) List<Long> idsExcluding) {
		List<AmpUserDto> users = null;
		
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

