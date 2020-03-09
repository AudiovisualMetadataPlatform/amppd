package edu.indiana.dlib.amppd.repository;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.test.web.servlet.MvcResult;



@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BundleRepositoryTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BundleRepository bundleRepository;
	
	
	@Before
	public void deleteAllBeforeTests() throws Exception {
		// TODO do a more refined delete to remove all data that might cause conflicts for tests in this class 
		// deleting all as below causes SQL FK violation when running the whole test suites, even though running this test class alone is fine,
		// probably due to the fact that some other tests call TestHelper to create the complete hierarchy of data entities from unit down to primaryfile
//		bundleRepository.deleteAll();
	}

	@Test
	public void shouldReturnRepositoryIndex() throws Exception {

		mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
				jsonPath("$._links.bundles").exists());
	}

	@Test
	public void shouldCreateBundle() throws Exception {

		mockMvc.perform(post("/bundles").content(
				"{\"name\": \"Bundle 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andExpect(
								header().string("Location", containsString("bundles/")));
	}

	@Test
	public void shouldRetrieveBundle() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/bundles").content(
				"{\"name\": \"Bundle 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Bundle 1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldQueryBundle() throws Exception {

		mockMvc.perform(post("/bundles").content(
				"{ \"name\": \"Bundle 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated());

		mockMvc.perform(
				get("/bundles/search/findByName?name={name}", "Bundle 1")).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.bundles[0].name").value(
										"Bundle 1"));
	}

	@Test
	public void shouldUpdateBundle() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/bundles").content(
				"{\"name\": \"Bundle 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(put(location).content(
				"{\"name\": \"Bundle 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Bundle 1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldPartiallyUpdateBundle() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/bundles").content(
				"{\"name\": \"Bundle 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(
				patch(location).content("{\"name\": \"Bundle 1.1.1\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Bundle 1.1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldDeleteBundle() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/bundles").content(
				"{ \"name\": \"Bundle 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(delete(location)).andExpect(status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isNotFound());
	}
}
