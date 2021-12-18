package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.service.MediaService;
import edu.indiana.dlib.amppd.util.TestHelper;

@Ignore
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class MediaControllerTests {

	@Autowired
	private MediaService mediaService;

	@Autowired
	private TestHelper testHelper;   
	
    @Autowired
    private MockMvc mvc;
	private String token = "";

	private Primaryfile primaryfile;
	
	@Before
	public void setup() {
		// prepare the primaryfile with empty symlink for testing
		primaryfile = testHelper.ensureTestAudio();
		primaryfile.setSymlink(null);
		token = testHelper.getToken();
	}

	@After
	public void cleanup() {
		// clean up all symlinks created
		mediaService.cleanup();
	}
	
    @Test
    public void shouldServePrimaryfile() throws Exception {  	
    	mvc.perform(get("/primaryfiles/{id}/media", primaryfile.getId()).header("Authorization", "Bearer " + token)).andExpect(
    			status().is3xxRedirection()).andExpect(
    					redirectedUrlPattern("http://*:8500/symlink/*"));
    }

}
