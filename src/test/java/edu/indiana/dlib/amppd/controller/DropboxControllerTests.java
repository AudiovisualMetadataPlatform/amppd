package edu.indiana.dlib.amppd.controller;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.service.DropboxService;
import edu.indiana.dlib.amppd.util.TestHelper;
import lombok.extern.slf4j.Slf4j;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
@Slf4j
public class DropboxControllerTests {

    @Autowired
    private MockMvc mvc;

	@Autowired
    private TestHelper testHelper;
	
	@Autowired
	private DropboxService dropboxService;

	private String token = "";
	private Collection collection = null;
	
	@Before
	public void setup() {
		token = testHelper.getToken();
		
		// ensure at least one collection exists for testing
		collection = testHelper.createTestCollection();		
	}
	
    @Test
    public void shouldCreateSubdirsForAllCollections() throws Exception {
    	mvc.perform(post("/dropbox/create").header("Authorization", "Bearer " + token)).andExpect(status().isOk());   
    	assertTrue(Files.exists(dropboxService.getDropboxPath(collection)));
    }
    
}
