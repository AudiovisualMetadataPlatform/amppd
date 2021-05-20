package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class BatchControllerTests {
	
	@Autowired
    private MockMvc mvc;
	
	String token = "";
	
	@Test
	public void shouldServeBatchIngestTemplateFile() throws Exception{
		MvcResult result= mvc.perform(get("/serveFile/{fileName:.+}","AmpBatchIngestTemplate.csv").header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andReturn();
		Assert.assertEquals(200, result.getResponse().getStatus());
		Assert.assertNotNull(result.getResponse().getContentAsString());
	}
	
	@Ignore
	@Test
	public void shouldNotServeFileForInvalidFilename() throws Exception {
		MvcResult result= mvc.perform(get("/serveFile/{fileName:.+}","NofileCheck").header("Authorization", "Bearer " + token)).andExpect(status().isNotFound()).andReturn();
		Assert.assertEquals(404, result.getResponse().getStatus());
		Assert.assertEquals(0,result.getResponse().getContentLength());
	}


}
