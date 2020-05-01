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
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ItemSupplementRepositoryTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ItemSupplementRepository itemSupplementRepository;

	@Autowired 
	private ObjectMapper mapper = new ObjectMapper();
	private ItemSupplement obj ;
	//private ObjectFactory objFactory = new ObjectFactory();

	@Autowired
    private TestHelper testHelper;
	String token = "";
	
	@BeforeClass
	public static void setupTest() 
	{
	    FixtureFactoryLoader.loadTemplates("edu.indiana.dlib.amppd.testData");
	}
	
	@Before
	public void deleteAllBeforeTests() throws Exception {
		// TODO do a more refined delete to remove all data that might cause conflicts for tests in this class 
		// deleting all as below causes SQL FK violation when running the whole test suites, even though running this test class alone is fine,
		// probably due to the fact that some other tests call TestHelper to create the complete hierarchy of data entities from unit down to primaryfile
//		itemSupplementRepository.deleteAll();
	    token = testHelper.getToken();
	}

	@Test
	public void shouldReturnItemSupplementRepositoryIndex() throws Exception {

		mockMvc.perform(get("/").header("Authorization", "Bearer " + token)).andDo(print()).andExpect(status().isOk()).andExpect(
				jsonPath("$._links.itemSupplements").exists());

	}
	
	@Test
	public void shouldCreateItemSupplement() throws Exception {

		mockMvc.perform(post("/itemSupplements").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"ItemSupplement 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andExpect(
								header().string("Location", containsString("itemSupplements/")));
	}
	
	@Test
	public void shouldRetrieveItemSupplement() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/itemSupplements").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"ItemSupplement 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("ItemSupplement 1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldQueryItemSupplement() throws Exception {

		obj = Fixture.from(ItemSupplement.class).gimme("valid");
		
		String json = mapper.writeValueAsString(obj);
		mockMvc.perform(post("/itemSupplements").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());

		mockMvc.perform(
				get("/itemSupplements/search/findByName?name={name}", obj.getName()).header("Authorization", "Bearer " + token)).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.itemSupplements[0].name").value(
										obj.getName()));
	}

	@Test
	public void shouldUpdateItemSupplement() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/itemSupplements").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"ItemSupplement 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(put(location).header("Authorization", "Bearer " + token).content(
				"{\"name\": \"ItemSupplement 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("ItemSupplement 1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldPartiallyUpdateItemSupplement() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/itemSupplements").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"ItemSupplement 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(
				patch(location).header("Authorization", "Bearer " + token).content("{\"name\": \"ItemSupplement 1.1.1\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("ItemSupplement 1.1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldDeleteItemSupplement() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/itemSupplements").header("Authorization", "Bearer " + token).content(
				"{ \"name\": \"ItemSupplement 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(delete(location).header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
	}
}
