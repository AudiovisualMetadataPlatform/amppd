package edu.indiana.dlib.amppd.service;


import java.util.Calendar;
import java.util.Random;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.Passwordresettoken;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.PasswordTokenRepository;
import edu.indiana.dlib.amppd.util.MD5Encryption;
import edu.indiana.dlib.amppd.util.TestHelper;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AmpUserServiceTests {
	@Autowired
	AmppdPropertyConfig amppdconfig = new AmppdPropertyConfig();
	
	@Autowired
    private AmpUserService ampUserService; // = new AmpUserServiceImpl(amppdconfig);
	
	@Autowired
	AmpUserRepository ampUserRepository;
	MD5Encryption md5;
	
	@Autowired
	private PasswordTokenRepository passwordTokenRepository;
	
	@Autowired
	private TestHelper unitTestHelper;
	
	@Test
    public void shouldEncryptPassword() throws Exception{
    	
	 	AmpUser user = getAmpUser();
	 	String raw_pswd = user.getPassword();
	 	
	 	ampUserService.registerAmpUser(user);
	 	AmpUser retrievedUser = new AmpUser();
	 	try {
			retrievedUser = ampUserRepository.findByUsername(user.getUsername()).get();
			Assert.assertFalse(retrievedUser.getPassword().equals(raw_pswd));
		  }
		  catch(Exception ex) {
			  System.out.println(ex.toString());
		  }
	 	
    }
	
	@Test
	public void shouldResetPassword() throws Exception{
    	
	 	AmpUser user = getAmpUser();  
	 	ampUserService.registerAmpUser(user);
	 	String old_pswd = MD5Encryption.getMd5(user.getPassword());
	 	String token = UUID.randomUUID().toString();
	 	Passwordresettoken myToken = new Passwordresettoken();
	 	myToken.setUser(user);
		myToken.setToken(token);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND,amppdconfig.getPasswordResetTokenExpiration());
		myToken.setExpiryDate(calendar.getTime());
		passwordTokenRepository.save(myToken);
	 	
	 	try { 
				ampUserService.resetPassword(user.getEmail(),"amp_new@123", token);
				AmpUser retrievedUser = ampUserRepository.findByEmail(user.getEmail()).get();
				Assert.assertFalse(retrievedUser.getPassword().equals(old_pswd));
		  }
		  catch(Exception ex) {
			  System.out.println(ex.toString());
		  }
    }
	
	@Test
	public void shouldApproveUser() throws Exception{	
	 	AmpUser user = getAmpUser();  
	 	//user.setPassword(md5.getMd5("amptest@123"));
	 	ampUserService.registerAmpUser(user);
	 	AmpUser user2 = ampUserRepository.findByEmail(user.getEmail()).get();
	 	if(user2 != null) {
	 		Long id = user2.getId();
	 		Assert.assertFalse(user.getStatus()==AmpUser.State.ACCEPTED);
	 		ampUserService.accountAction(id, "approve");
	 		user2 = ampUserRepository.findByEmail(user.getEmail()).get();
	 		Assert.assertTrue(user2.getStatus()==AmpUser.State.ACCEPTED);
	 	}
    }
	
	@Test
	public void shouldActivateUser() throws Exception{	
	 	AmpUser user = getAmpUser();  
	 	ampUserService.registerAmpUser(user);
	 	//String old_pswd = MD5Encryption.getMd5(user.getPassword());
	 	String token = UUID.randomUUID().toString();
	 	Passwordresettoken myToken = new Passwordresettoken();
	 	myToken.setUser(user);
		myToken.setToken(token);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.SECOND,amppdconfig.getAccountActivationTokenExpiration());
		myToken.setExpiryDate(calendar.getTime());
		passwordTokenRepository.save(myToken);
		Assert.assertFalse(user.getStatus()==AmpUser.State.ACTIVATED);
	 	try { 
	 		ampUserRepository.updateStatus(user.getId(), AmpUser.State.ACCEPTED);
			ampUserService.activateAccount(token);
			AmpUser retrievedUser = ampUserRepository.findByEmail(user.getEmail()).get();
			Assert.assertTrue(retrievedUser.getStatus()==AmpUser.State.ACTIVATED);
		  }
		  catch(Exception ex) {
			  System.out.println(ex.toString());
		  }
    }
	
	private AmpUser getAmpUser() {
        Random rand = new Random(); 
        int rand_int1 = rand.nextInt(1000); 
    	AmpUser ampUser = new AmpUser();
    	
    	ampUser.setPassword("password1234");
    	ampUser.setUsername("testUser_" + rand_int1);
    	ampUser.setEmail(ampUser.getUsername()+"@iu.edu");
    	
    	return ampUser;
    }
	@Before
	public void before() {
		unitTestHelper.deleteAllUsers();
	}
	@After
	public void after() {
		unitTestHelper.deleteAllUsers();
	}
}
