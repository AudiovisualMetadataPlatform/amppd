package edu.indiana.dlib.amppd.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;


@Service
public class AmpUserService {

	   
	  
	  @Autowired
	  private AmpUserRepository ampUserRepository;
	  
	  public boolean validate(String username, String pswd) { 
		  String userFound = ampUserRepository.findByUsername(username, pswd);  
		  if(userFound != null)
		  {
			  if(userFound.equals("1"))
				  return true;
		  }
		  return false;
	  }
	  
	  public int registerAmpUser(AmpUser user) { 
		  user = ampUserRepository.save(user);
		  if(user.getId() > 0)
			  return 1;  
		  else
			  return 0;
	  }
	  

}
