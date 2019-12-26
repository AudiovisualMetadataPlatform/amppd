package edu.indiana.dlib.amppd.controller;
  
  import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.User;
import edu.indiana.dlib.amppd.service.impl.UserServiceImpl;
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
  public class UserController{
	  @Autowired
	  private UserServiceImpl ampService;
	  
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
			@RequestBody User user){ 
		log.info("Registeration for User=> Name:"+ user.getUsername());	
		AuthResponse res = ampService.registerUser(user);
		log.info(" Registeration result: " + res);
		return res;
	  }

	 
  }
		 