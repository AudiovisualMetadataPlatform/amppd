package edu.indiana.dlib.amppd.controller;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.security.JwtTokenUtil;
import edu.indiana.dlib.amppd.service.DropboxService;
import edu.indiana.dlib.amppd.util.TestHelper;

@Ignore
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
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
		
		// clean up all existing collections in case those created by other tests don't have unit populated
		testHelper.cleanupCollections();
		
		// ensure at least one collection exists for testing
		collection = testHelper.createTestCollection();		
	}
	
    @Test
    public void shouldCreateSubdirsForAllCollections() throws Exception {
    	mvc.perform(post("/dropbox/create").header("Authorization", JwtTokenUtil.JWT_AUTH_PREFIX + token)).andExpect(status().isOk());   
    	assertTrue(Files.exists(dropboxService.getSubDirPath(collection)));
    }
    
}
