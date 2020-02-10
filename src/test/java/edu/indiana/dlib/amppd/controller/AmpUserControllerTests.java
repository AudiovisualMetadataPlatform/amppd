package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
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
	
	@Before
	public void init() {
		testHelper.deleteAllUsers();
	}

	@After
	public void cleanup() {
		testHelper.deleteAllUsers();
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
    	
    	String url = String.format("/login");
    	AuthRequest request = new AuthRequest();
    	request.setEmailid(user.getEmail());
    	request.setPassword(user.getPassword());
    	String json = mapper.writeValueAsString(request);
    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(true));
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
        
    	String url = String.format("/login");
    	AuthRequest request = new AuthRequest();
    	request.setEmailid(user.getEmail());
    	request.setPassword(user.getPassword());
    	String json = mapper.writeValueAsString(request);
    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(false));
    	
    	
    	ampUserService.approveUser(user.getUsername());

    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(true));
    	
    }
    //Test if register sends an email for approval
    @Test
	public void shouldTestApproveUser() throws Exception {
    	AmpUser user = getAmpUser(); 
    	user.setEmail("amppdiu@gmail.com"); 
    	postRegister(user, true);
    	String url = String.format("/login");
    	AuthRequest request_login = new AuthRequest();
    	request_login.setEmailid(user.getEmail());
    	request_login.setPassword(user.getPassword());
    	String json1 = mapper.writeValueAsString(request_login);
    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json1)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(false));
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
    	ampUser.setUsername("testUser_" + rand_int1);
    	ampUser.setEmail(ampUser.getUsername()+"@iu.edu");
    	
    	return ampUser;
    }
    
    private void postRegister(AmpUser user, boolean expectSuccess) throws Exception{
    	String json = mapper.writeValueAsString(user);
    	
    	mvc.perform(post("/register")
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(expectSuccess));
    	
    }
    
}
