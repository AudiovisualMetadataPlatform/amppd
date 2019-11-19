package edu.indiana.dlib.amppd.controller;
  
  import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.impl.AmpUserService;
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
	  private AmpUserService ampService;
	  private static AmpUser user;
	  
	  @GetMapping("/amp/auth") 
	  public @ResponseBody boolean loginAuth(
			@RequestParam("name") String name,
			@RequestParam("pswd") String pswd){ 
		boolean res = false;
		log.info("Login Authenticaton for User=> Name:"+ name);	
		res = ampService.validate(name, pswd);
		log.info(" Authenticaton result:"+res);
		return res;
	  }
	  
	  @GetMapping("/amp/register") 
	  public @ResponseBody boolean register(
			@RequestParam("name") String name,
			@RequestParam("pswd") String pswd){ 
		boolean res = false;
		log.info("Registeration for User=> Name:"+ name);	
		user = new AmpUser();
		user.setUsername(name); 
		user.setPassword(pswd);
		//user.setApproved(false);
		res = ampService.registerAmpUser(user);
		log.info(" Registeration result:"+res);
		return res;
	  }
	  
  
	/*
	 * @PostMapping("/register") String register(@RequestParam(value="name") String
	 * name) { return "redirect:/home"; }
	 * 
	 * @GetMapping("/welcome") public String welcome(Model model) { return "home"; }
	 */
	 
  }
		 