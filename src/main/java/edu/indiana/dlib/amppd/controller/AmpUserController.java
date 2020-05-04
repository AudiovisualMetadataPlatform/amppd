package edu.indiana.dlib.amppd.controller;
  
  import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.config.JwtTokenUtil;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.impl.AmpUserServiceImpl;
import edu.indiana.dlib.amppd.web.AuthRequest;
import edu.indiana.dlib.amppd.web.AuthResponse;
import edu.indiana.dlib.amppd.web.JwtRequest;
import edu.indiana.dlib.amppd.web.JwtResponse;
import lombok.extern.slf4j.Slf4j;
  
 /**
	 * Controller for REST operations on Login.
	 * 
	 * @author vinitab
	 *
	 */
  @CrossOrigin(origins = "*", allowedHeaders = "*")
  @RestController
  @Slf4j
  public class AmpUserController{
	  @Autowired
	  private AmpUserServiceImpl ampService;
	  @Autowired
	  private AuthenticationManager authenticationManager;

	  @Autowired
	  private JwtTokenUtil jwtTokenUtil;
	  
	  @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
	  public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
		  AuthResponse response = ampService.validate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
			
		  if(!response.isSuccess()) {
			  return ResponseEntity.status(400).body(null);
		  }
			
		  final AmpUser userDetails = ampService.getUser(authenticationRequest.getUsername());
		  final String token = jwtTokenUtil.generateToken(userDetails);
		  return ResponseEntity.ok(new JwtResponse(token));
	  }
		
	  @RequestMapping(value = "/validate", method = RequestMethod.POST)
	  public ResponseEntity<?> validateToken() throws Exception {
		  return ResponseEntity.ok("Success");
	  }

	  @PostMapping(path = "/register", consumes = "application/json", produces = "application/json")
	  public @ResponseBody AuthResponse register(
			@RequestBody AmpUser user){ 
		log.info("Registeration for User=> Name:"+ user.getUsername());	
		log.info("Registeration for User=> Email:"+ user.getEmail());	
		AuthResponse res = ampService.registerAmpUser(user);
		log.info(" Registeration result: " + res);
		return res;
	  }

	  @PostMapping(path = "/forgot-password", consumes = "application/json", produces = "application/json")
	  public @ResponseBody AuthResponse forgotPassword(
			  @RequestBody AuthRequest request){ 
		log.info("Forgot Password for User=> Email:"+ request.getEmailid());	
		AuthResponse res = ampService.emailToken(request.getEmailid());
		log.info(" Forgot Password result: " + res);
		return res;
	  }
	  
	  @PostMapping(path = "/reset-password", consumes = "application/json", produces = "application/json")
	  public @ResponseBody AuthResponse resetPassword(
			  @RequestBody AuthRequest request){ 
		log.info("Reset Password for User=> Email:"+ request.getEmailid());	
		AuthResponse res = ampService.resetPassword(request.getEmailid(), request.getPassword(), request.getToken());
		log.info(" Reset Password result: " + res);
		return res;
	  }
	  
	  @PostMapping(path = "/approve-user", consumes = "application/json", produces = "application/json")
	  public @ResponseBody AuthResponse approveUser(
			  @RequestBody AuthRequest request){ 
		log.info("Approve User=> id:"+ request.getUserId());	
		AuthResponse res = ampService.approveUser(request.getUserId());
		log.info(" Reset Password result: " + res);
		return res;
	  }
	  
	  @PostMapping(path = "/reset-password-getEmail", consumes = "application/json", produces = "application/json")
	  public @ResponseBody AuthResponse resetPasswordGetEmail(
			  @RequestBody AuthRequest request){ 
		log.info("Calling get email for a token using resetPasswordGetEmail()");	
		AuthResponse res = ampService.resetPasswordGetEmail(request.getToken());
		log.info(" Fetched Email id for a token using resetPasswordGetEmail():"+res.getEmailid());
		return res;
	  }
  }
		 
