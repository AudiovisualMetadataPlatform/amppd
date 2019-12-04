package edu.indiana.dlib.amppd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.indiana.dlib.amppd.config.EncryptionConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.service.AmpUserService;


@Service
public class AmpUserServiceImpl implements AmpUserService{

	  @Autowired
	  private AmpUserRepository ampUserRepository;
	  
	  
	/*
	 * @Autowired private JavaMailSender javaMailSender;
	 */
	  
	  public boolean validate(String username, String pswd) { 
		  
		  String pswdFound = ampUserRepository.findByUsername(username);
		  String pswdExpected = EncryptionConfig.getMd5(username+pswd);
		  if(pswdFound.equals(pswdExpected))
		  {
			  //if(userFound.equals("1"))
			  return true;
		  }
		  return false;
	  }
	  
	  public boolean registerAmpUser(String name, String pswd) { 
		  AmpUser user = new AmpUser();
		  user.setUsername(name); 
		  user.setPassword(EncryptionConfig.getMd5(name+pswd));
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