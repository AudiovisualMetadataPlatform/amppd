package edu.indiana.dlib.amppd.service.impl;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;

import org.springframework.mail.SimpleMailMessage;


@Service
public class AmpUserServiceImpl implements AmpUserService{

	  @Autowired
	  private AmpUserRepository ampUserRepository;
	  
	  @Autowired
	  private JavaMailSender javaMailSender;
	  
	  public boolean validate(String username, String pswd) { 
		  String userFound = ampUserRepository.findByUsername(username, pswd);  
		  if(userFound != null)
		  {
			  if(userFound.equals("1"))
				  return true;
		  }
		  return false;
	  }
	  
	  public boolean registerAmpUser(AmpUser user) { 
		  user = ampUserRepository.save(user);
		  //sendEmail(user);
		  if(user.getId() > 0)
			  return true;  
		  return false;
	  }

	@Override
	public void sendEmail(AmpUser u) {
		// TODO Auto-generated method stub
	  
		/*
		 * { SimpleMailMessage msg = new SimpleMailMessage();
		 * msg.setTo("vinitaboolchandani@gmail.com");
		 * msg.setSubject("New User Registration Pending");
		 * msg.setText("Hi, The following user has requested approval user name:"+u.
		 * getUsername()); javaMailSender.send(msg);
		 * 
		 * }
		 */
	}

}