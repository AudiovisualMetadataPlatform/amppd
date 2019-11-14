
  package edu.indiana.dlib.amppd.controller;
  
  import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	  
	  @GetMapping("/amp/auth") 
	  public boolean loginAuth(
			@RequestParam("name") String name,
			@RequestParam("pswd") String pswd){ 
		boolean res = false;
		log.info("Login Authenticaton for User=> Name:"+ name +"Password:"+pswd);	
		res = ampService.validate(name, pswd);
		log.info(" Authenticaton result:"+res);
		return res;
	  }
	  
	  @PostMapping("/amp/register") 
	  public boolean register(
			@RequestParam("name") String name,
			@RequestParam("pswd") String pswd){ 
		boolean res = false;
		log.info("Registeration for User=> Name:"+ name +"Password:"+pswd);	
		res = ampService.registerAmpUser(name, pswd);
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
		 