package edu.indiana.dlib.amppd.controller;
  
  import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.impl.AmpUserServiceImpl;
import edu.indiana.dlib.amppd.web.AuthRequest;
import edu.indiana.dlib.amppd.web.AuthResponse;
import lombok.extern.java.Log;
  
 /**
	 * Controller for REST operations on Login.
	 * 
	 * @author vinitab
	 *
	 */
  @CrossOrigin(origins = "*")
  @RestController
  @Log 
  public class AmpUserController{
	  @Autowired
	  private AmpUserServiceImpl ampService;
	  
	  @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
	  public @ResponseBody AuthResponse loginAuth(@RequestBody AuthRequest request){ 
		AuthResponse res = new AuthResponse();
		log.info("Login Authenticaton for User=> Name:"+ request.getUsername());	
		res = ampService.validate(request.getUsername(), request.getPassword());
		log.info(" Authenticaton result:"+res);
		return res;
	  }

	  @PostMapping(path = "/register", consumes = "application/json", produces = "application/json")
	  public @ResponseBody AuthResponse register(
			@RequestBody AmpUser user){ 
		log.info("Registeration for User=> Name:"+ user.getUsername());	
		AuthResponse res = ampService.registerAmpUser(user);
		log.info(" Registeration result: " + res);
		return res;
	  }

	  @PostMapping(path = "/forgot-password", consumes = "application/json", produces = "application/json")
	  public @ResponseBody AuthResponse forgotPassword(
			  @RequestBody AuthRequest request){ 
		log.info("Reset Password for User=> Email:"+ request.getEmailid());	
		AuthResponse res = ampService.emailToken(request.getEmailid());
		log.info(" Reset Password result: " + res);
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
  }
		 