package edu.indiana.dlib.amppd.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class WorkflowControllerTests {

    @Autowired
    private MockMvc mvc;

    // TODO remove @Ignore once Galaxy is set up for CI env
    @Ignore
    @Test
    public void shouldReturnWorkflows() throws Exception {
    	mvc.perform(get("/workflows")).andExpect(status().isOk()).andExpect(
//				jsonPath("$", hasSize(1))).andExpect(	// TODO need to import org.hamcrest.Matchers.hasSize with added dependency hamcrest-all
						jsonPath("$[0].model_class").value("StoredWorkflow"));
    }

}
