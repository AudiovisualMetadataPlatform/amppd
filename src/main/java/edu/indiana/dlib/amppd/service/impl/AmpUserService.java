package edu.indiana.dlib.amppd.service.impl;

import org.springframework.stereotype.Service;
import edu.indiana.dlib.amppd.model.AmpUser;

@Service
public class AmpUserService {

	  private static AmpUser user = new AmpUser(); 
	  static {
		 
		  user.setUsername("amp"); 
		  user.setPassword("tmppass"); 
	  }
	  
	  public boolean validate(String name, String pswd) { 
		  if (name.equals(user.getUsername()) && pswd.equals(user.getPassword()))
			  return true; 
		  return false;
	  }
	  
	  public boolean registerAmpUser(String name, String pswd) { 
		  if (name.equals(user.getUsername()) && pswd.equals(user.getPassword()))
			  return true; 
		  return false;
	  }
	  

}
