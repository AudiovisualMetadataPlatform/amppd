package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class DropboxControllerTests {

    @Autowired
    private MockMvc mvc;

	@Autowired
    private TestHelper testHelper;
	String token = "";
	
	@Before
	public void setup() {
		token = testHelper.getToken();
	}
	
    @Test
    public void shouldCreateSubdirsForAllCollections() throws Exception {
    	mvc.perform(post("/dropbox/create").header("Authorization", "Bearer " + token)).andExpect(status().isOk());   
    }
    
}
