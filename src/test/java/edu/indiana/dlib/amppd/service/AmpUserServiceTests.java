package edu.indiana.dlib.amppd.service;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.junit4.SpringRunner;
import edu.indiana.dlib.amppd.model.AmpUser;

import edu.indiana.dlib.amppd.repository.AmpUserRepository;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AmpUserServiceTests {
	
	@Autowired
    private AmpUserService ampUserService;
	
	AmpUserRepository ampUserRepository;
	
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
}
