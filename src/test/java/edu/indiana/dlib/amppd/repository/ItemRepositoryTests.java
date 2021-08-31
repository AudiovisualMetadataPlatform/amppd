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

import org.apache.commons.lang.StringUtils;
import org.hamcrest.core.StringContains;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ItemRepositoryTests {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
    private TestHelper testHelper;
		
	@Autowired 
	private ObjectMapper mapper;
	
	private Item item ;	
	private String token = "";
	
	@BeforeClass
	public static void setupTest() {
	    FixtureFactoryLoader.loadTemplates("edu.indiana.dlib.amppd.data");
	}
	
	@Before
	public void deleteAllBeforeTests() throws Exception {
		// TODO do a more refined delete to remove all data that might cause conflicts for tests in this class 
		// deleting all as below causes SQL FK violation when running the whole test suites, even though running this test class alone is fine,
		// probably due to the fact that some other tests call TestHelper to create the complete hierarchy of data entities from unit down to primaryfile
//		itemRepository.deleteAll();
	    token = testHelper.getToken();
	}

	@Test
	public void shouldReturnRepositoryIndex() throws Exception {
		mockMvc.perform(get("/").header("Authorization", "Bearer " + token)).andDo(print()).andExpect(status().isOk()).andExpect(
				jsonPath("$._links.items").exists());
	}

	@Test
	public void shouldCreateItem() throws Exception {
		mockMvc.perform(post("/items").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"Item 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andExpect(
								header().string("Location", containsString("items/")));
	}

	@Test
	public void shouldRetrieveItem() throws Exception {
		MvcResult mvcResult = mockMvc.perform(post("/items").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"Item 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Item 1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldQueryItemDescription() throws Exception {
		item = Fixture.from(Item.class).gimme("valid");
		
		String json = mapper.writeValueAsString(item);
		mockMvc.perform(post("/items").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		
		mockMvc.perform(
				get("/items/search/findByDescription?description={description}", item.getDescription()).header("Authorization", "Bearer " + token))
					.andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.items[0].description").value(
										item.getDescription()));
	}
	
	@Test
	public void shouldQueryItemCreatedBy() throws Exception {
		item = Fixture.from(Item.class).gimme("valid");
		
		String json = mapper.writeValueAsString(item);
		mockMvc.perform(post("/items").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		
//		String username = ampUserservice.getCurrentUsername();
		mockMvc.perform(
				get("/items/search/findByCreatedBy?createdBy={createdBy}", TestHelper.TEST_USER).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.items[0].createdBy").value(
										TestHelper.TEST_USER));
	}
	
	@Test
	public void shouldQueryItemNameKeyword() throws Exception {
		item = Fixture.from(Item.class).gimme("valid");
		String[] words = StringUtils.split(item.getName());
		String keyword = words[words.length-1];
		
		String json = mapper.writeValueAsString(item);
		mockMvc.perform(post("/items").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		
		mockMvc.perform(
				get("/items/search/findByKeyword?keyword={keyword}", keyword).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.items[0].name").value(new StringContains(keyword)));
	}

	@Test
	public void shouldQueryItemNameKeywordCaseInsensitive() throws Exception {
		item = Fixture.from(Item.class).gimme("valid");
		String[] words = StringUtils.split(item.getName());
		String keyword = words[words.length-1].toUpperCase();
		
		String json = mapper.writeValueAsString(item);
		mockMvc.perform(post("/items").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		
		mockMvc.perform(
				get("/items/search/findByKeyword?keyword={keyword}", keyword).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.items[0].name").value(new StringContains(keyword)));
		
		keyword = words[words.length-1].toLowerCase();
		
		mockMvc.perform(
				get("/items/search/findByKeyword?keyword={keyword}", keyword).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.items[0].name").value(new StringContains(keyword)));
	}
	
	@Test
	public void shouldQueryItemDescriptionKeyword() throws Exception {
		item = Fixture.from(Item.class).gimme("valid");
		String[] words = StringUtils.split(item.getDescription());
		String keyword = words[words.length-1];
		
		String json = mapper.writeValueAsString(item);
		mockMvc.perform(post("/items").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		
		mockMvc.perform(
				get("/items/search/findByKeyword?keyword={keyword}", keyword).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.items[0].description").value(new StringContains(keyword)));
	}	

	@Test
	public void shouldUpdateItem() throws Exception {
		MvcResult mvcResult = mockMvc.perform(post("/items").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"Item 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(put(location).content(
				"{\"name\": \"Item 1.1\", \"description\":\"For test\"}").header("Authorization", "Bearer " + token)).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Item 1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldPartiallyUpdateItem() throws Exception {
		MvcResult mvcResult = mockMvc.perform(post("/items").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"Item 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(
				patch(location).header("Authorization", "Bearer " + token).content("{\"name\": \"Item 1.1.1\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Item 1.1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldDeleteItem() throws Exception {
		MvcResult mvcResult = mockMvc.perform(post("/items").header("Authorization", "Bearer " + token).content(
				"{ \"name\": \"Item 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(delete(location).header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
	}
}
