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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CollectionSupplementRepositoryTests {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired 
	private TestHelper testHelper;

	@Autowired 
	private ObjectMapper mapper = new ObjectMapper();
	
	private CollectionSupplement supplement;
	private String token;

	
	@BeforeClass
	public static void setupTest() {
	    FixtureFactoryLoader.loadTemplates("edu.indiana.dlib.amppd.fixture");
	}
	
	@Before
	public void deleteAllBeforeTests() throws Exception {
		// TODO do a more refined delete to remove all data that might cause conflicts for tests in this class 
		// deleting all as below causes SQL FK violation when running the whole test suites, even though running this test class alone is fine,
		// probably due to the fact that some other tests call TestHelper to create the complete hierarchy of data entities from unit down to primaryfile
//		supplementRepository.deleteAll();
		token = "Bearer " + testHelper.getToken();
	}

	@Test
	public void shouldReturnCollectionSupplementRepositoryIndex() throws Exception {
		mockMvc.perform(get("/").header("Authorization", token)).andDo(print()).andExpect(status().isOk()).andExpect(
				jsonPath("$._links.collectionSupplements").exists());

	}
	
	@Test
	public void shouldCreateCollectionSupplement() throws Exception {

		mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(
				"{\"name\": \"CollectionSupplement 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andExpect(
								header().string("Location", containsString("collectionSupplements/")));
	}
	
	@Test
	public void shouldRetrieveCollectionSupplement() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(
				"{\"name\": \"CollectionSupplement 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("CollectionSupplement 1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldQueryCollectionSupplement() throws Exception {
		
		supplement = Fixture.from(CollectionSupplement.class).gimme("valid");
		
		String json = mapper.writeValueAsString(supplement);
		mockMvc.perform(post("/collectionSupplements").header("Authorization", token)
				  .content(json)).andExpect(
						  status().isCreated());
		mockMvc.perform(
				get("/collectionSupplements/search/findByName?name={name}", supplement.getName()).header("Authorization", token)).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.collectionSupplements[0].name").value(
										supplement.getName()));
	}

	@Test
	public void shouldUpdateCollectionSupplement() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(
				"{\"name\": \"CollectionSupplement 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(put(location).header("Authorization", token).content(
				"{\"name\": \"CollectionSupplement 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("CollectionSupplement 1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldPartiallyUpdateCollectionSupplement() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(
				"{\"name\": \"CollectionSupplement 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(
				patch(location).header("Authorization", token).content("{\"name\": \"CollectionSupplement 1.1.1\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("CollectionSupplement 1.1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldDeleteCollectionSupplement() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(
				"{ \"name\": \"CollectionSupplement 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
}
