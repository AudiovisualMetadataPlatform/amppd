package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.web.AuthRequest;
import edu.indiana.dlib.amppd.web.JwtRequest;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class AmpUserControllerTests {

    @Autowired
    private MockMvc mvc;
	
	@Autowired
    private AmpUserService ampUserService;
	
	@Autowired private ObjectMapper mapper;
	@Autowired private TestHelper testHelper;
	String token = "";
	
	@Before
	public void init() {
		testHelper.deleteAllUsers();
		token = testHelper.getToken();
	}

	@After
	public void cleanup() {
		testHelper.deleteAllUsers();
	}
	
	@Test
	public void shouldReturnToken() throws Exception {
		AmpUser user = testHelper.createTestUser();
    	    	
    	String url = String.format("/authenticate");
    	JwtRequest request = new JwtRequest();
    	request.setUsername(user.getUsername());
    	request.setPassword(user.getPassword());
    	String json = mapper.writeValueAsString(request);
    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("token").isString());
	}
		
    @Test
    public void shouldRejectInvalidEmail() throws Exception {
    	AmpUser user = getAmpUser();

    	postRegister(user, true);
    	
    	user.setEmail("@yahoo.com");
    	postRegister(user, false);

    	user.setEmail("john@");
    	postRegister(user, false);
    	
    	user.setEmail(".@yahoo");
    	postRegister(user, false);
    }
    
    @Test
    public void shouldRejectShortPassword() throws Exception {
    	AmpUser user = getAmpUser();
    	user.setPassword("123456");
    	postRegister(user, false);
   
    }
    @Test
    public void shouldRejectShortUsername() throws Exception {
    	AmpUser user = getAmpUser();
    	user.setUsername(user.getUsername().substring(0,2));
    	postRegister(user, false);
    }
    @Test
    public void shouldRejectEmptyPassword() throws Exception {
    	AmpUser user = getAmpUser();
    	user.setPassword("");
    	postRegister(user, false);
   }
    
    @Test
    public void shouldEncryptPassword() throws Exception {
    	AmpUser user = getAmpUser();
    	postRegister(user, true);   //saves the password in database after encryption
    	
    	ampUserService.approveUser(user.getUsername());
    	
    	String url = String.format("/authenticate");
    	JwtRequest request = new JwtRequest();
    	request.setUsername(user.getEmail());
    	request.setPassword(user.getPassword());
    	String json = mapper.writeValueAsString(request);
    	mvc.perform(post(url).header("Authorization", "Bearer " + token)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("token").isString());
   }

    @Test
    public void shouldRejectEmptyUsername() throws Exception {
    	AmpUser user = getAmpUser();
    	user.setUsername("");
    	postRegister(user, false);
    }  

    @Test
    public void shouldAcceptValidUsernamePasswordEmail() throws Exception {
    	AmpUser user = getAmpUser();
    	postRegister(user, true);
    	
    }
    @Test
    public void shouldValidateApprovedUser() throws Exception {
    	AmpUser user = getAmpUser();
    	postRegister(user, true);
        
    	String url = String.format("/authenticate");
    	JwtRequest request = new JwtRequest();
    	request.setUsername(user.getUsername());
    	request.setPassword(user.getPassword());
    	String json = mapper.writeValueAsString(request);
    	mvc.perform(post(url).header("Authorization", "Bearer " + token)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().is(400));
    	
    	
    	ampUserService.approveUser(user.getUsername());

    	mvc.perform(post(url).header("Authorization", "Bearer " + token)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("token").isString());
    	
    }
    //Test if register sends an email for approval
    @Test
	public void shouldTestApproveUser() throws Exception {
    	AmpUser user = getAmpUser(); 
    	user.setEmail("amppdiu@gmail.com"); 
    	postRegister(user, true);
    	String url = String.format("/authenticate");
    	JwtRequest request_login = new JwtRequest();
    	request_login.setUsername(user.getEmail());
    	request_login.setPassword(user.getPassword());
    	String json1 = mapper.writeValueAsString(request_login);
    	mvc.perform(post(url).header("Authorization", "Bearer " + token)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json1)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().is(400));
    	/*
    	url = String.format("/approve-user");
    	//ampUserService.approveUser(user.getUsername());
    	AuthRequest request_approve = new AuthRequest();
    	request_approve.setId(user.getId());
    	String json2 = mapper.writeValueAsString(request_approve);
    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json2)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(true));
    	
    	url = String.format("/login");
    	//json = mapper.writeValueAsString(request_login);
    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json1)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(true));*/
    }
    
    @Test
	public void testForgotPasswordEmail() throws Exception {
    	AmpUser user = getAmpUser(); 
    	String url = String.format("/forgot-password");
    	user.setEmail("amppdiu@gmail.com"); 
    	postRegister(user, true);
    	
    	ampUserService.approveUser(user.getUsername());
    	AuthRequest request = new AuthRequest();
    	request.setEmailid(user.getEmail());
    	
    	String json = mapper.writeValueAsString(request);
    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(true));
    }

    private AmpUser getAmpUser() {
        Random rand = new Random(); 
        int rand_int1 = rand.nextInt(1000); 
    	AmpUser ampUser = new AmpUser();
    	ampUser.setPassword("password1234");
    	ampUser.setUsername("testUser_" + rand_int1+"@iu.edu");
    	ampUser.setEmail(ampUser.getUsername());
    	
    	return ampUser;
    }
    
    private void postRegister(AmpUser user, boolean expectSuccess) throws Exception{
    	String json = mapper.writeValueAsString(user);
    	
    	mvc.perform(post("/register").header("Authorization", "Bearer " + token)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(expectSuccess));
    	
    }
    
}
