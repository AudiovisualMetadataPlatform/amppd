package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Random;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.indiana.dlib.amppd.model.User;
import edu.indiana.dlib.amppd.service.UserService;
import edu.indiana.dlib.amppd.web.AuthRequest;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class UserControllerTests {

    @Autowired
    private MockMvc mvc;
	
	@Autowired
    private UserService userService;
	
	@Autowired private ObjectMapper mapper;
	
    @Test
    public void shouldRejectInvalidEmail() throws Exception {
    	User user = getUser();

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
    	User user = getUser();
    	user.setPassword("123456");
    	postRegister(user, false);
   
    }
    @Test
    public void shouldRejectShortUsername() throws Exception {
    	User user = getUser();
    	user.setUsername(user.getUsername().substring(0,2));
    	postRegister(user, false);
    }
    @Test
    public void shouldRejectEmptyPassword() throws Exception {
    	User user = getUser();
    	user.setPassword("");
    	postRegister(user, false);
   }

    @Test
    public void shouldRejectEmptyUsername() throws Exception {
    	User user = getUser();
    	user.setUsername("");
    	postRegister(user, false);
    }

    @Test
    public void shouldAcceptValidUsernamePasswordEmail() throws Exception {
    	User user = getUser();
    	postRegister(user, true);
    	
    }
    @Test
    public void shouldValidateApprovedUser() throws Exception {
    	User user = getUser();
    	postRegister(user, true);
        
    	String url = String.format("/login");
    	AuthRequest request = new AuthRequest();
    	request.setUsername(user.getUsername());
    	request.setPassword(user.getPassword());
    	String json = mapper.writeValueAsString(request);
    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(false));
    	
    	
    	userService.approveUser(user.getUsername());

    	mvc.perform(post(url)
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(true));
    	
    }

    private User getUser() {
        Random rand = new Random(); 
        int rand_int1 = rand.nextInt(1000); 
    	User user = new User();
    	user.setEmail("test@iu.edu");
    	user.setPassword("password1234");
    	user.setUsername("testUser_" + rand_int1);
    	return user;
    }
    
    private void postRegister(User user, boolean expectSuccess) throws Exception{
    	String json = mapper.writeValueAsString(user);
    	
    	mvc.perform(post("/register")
    		       .contentType(MediaType.APPLICATION_JSON)
    		       .content(json)
    		       .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.success").isBoolean()).andExpect(jsonPath("$.success").value(expectSuccess));
    	
    }
    
}
