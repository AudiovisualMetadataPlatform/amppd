package edu.indiana.dlib.amppd.service;


import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.PasswordResetToken;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.PasswordTokenRepository;
import edu.indiana.dlib.amppd.util.MD5Encryption;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AmpUserServiceTests {
	
	@Autowired
    private AmpUserService ampUserService;
	
	AmpUserRepository ampUserRepository;
	MD5Encryption md5;
	
	@Autowired
	private PasswordTokenRepository passwordTokenRepository;
	
	@Test
    public void shouldEncryptPassword() throws Exception{
    	
	 	AmpUser user = new AmpUser();
	 	user.setUsername("ampEncryptionTestID1");
	 	user.setPassword("ampEncryptionPswd@123");
	 	user.setEmail("ampEncryptionId1@iu.edu");
	 	
	 	ampUserService.registerAmpUser(user);
	 	AmpUser retrievedUser = new AmpUser();
	 	try {
			retrievedUser = ampUserRepository.findByUsername(user.getUsername()).orElseThrow(() -> new RuntimeException("User not found: " + user.getUsername()));
			Assert.assertFalse(retrievedUser.getPassword().equals(user.getPassword()));
		  }
		  catch(Exception ex) {
			  System.out.println(ex.toString());
		  }
	 	
    }
	
	@Test
	public void shouldSendEmail() throws Exception{
    	
	 	AmpUser user = new AmpUser();
	 	user.setUsername("ampPasswordResetTest1");
	 	user.setPassword(md5.getMd5("amp@123"));
	 	user.setEmail("vinitaboolchandani@gmail.com");
	 	String token = UUID.randomUUID().toString();
	 	PasswordResetToken myToken = new PasswordResetToken();
	    myToken.setToken(token);
	    myToken.setUser(user);
	    passwordTokenRepository.save(myToken); 
	    
	 	ampUserService.registerAmpUser(user);
	 	AmpUser retrievedUser = new AmpUser();
	 	try {
				retrievedUser = ampUserRepository.findByEmail(user.getEmail()).orElseThrow(() -> new RuntimeException("User not found: " + user.getEmail()));
				//Assert.assertFalse(retrievedUser.getPassword().equals(user.getPassword()));
				ampUserService.resetPassword(retrievedUser.getEmail(), md5.getMd5("amp_new@123"), token);
		  }
		  catch(Exception ex) {
			  System.out.println(ex.toString());
		  }
	 	
    }
}
