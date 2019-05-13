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

import java.util.HashMap;

import org.junit.Before;
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

import edu.indiana.dlib.amppd.model.factory.ObjectFactory;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.repository.ItemRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ItemRepositoryTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ItemRepository itemRepository;
	
	@Autowired 
	private ObjectMapper mapper;
	private Item objItem ;
	private ObjectFactory objFactory = new ObjectFactory();
	
	@Before
	public void initiateBeforeTests() throws ClassNotFoundException
	{
		HashMap<String, String> params = new HashMap<String, String>();
		objItem= (Item)objFactory.createDataentityObject(params, "Item");
		
	}
	
	@Before
	public void deleteAllBeforeTests() throws Exception {
		itemRepository.deleteAll();
	}

	@Test
	public void shouldReturnRepositoryIndex() throws Exception {

		mockMvc.perform(get("/")).andDo(print()).andExpect(status().isOk()).andExpect(
				jsonPath("$._links.items").exists());
	}

	@Test
	public void shouldCreateItem() throws Exception {

		mockMvc.perform(post("/items").content(
				"{\"name\": \"Item 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andExpect(
								header().string("Location", containsString("items/")));
	}

	@Test
	public void shouldRetrieveItem() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/items").content(
				"{\"name\": \"Item 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Item 1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldQueryItem() throws Exception {

		  mockMvc.perform(post("/items").content(
		  "{ \"name\": \"Item 1\", \"description\":\"For test\"}")).andExpect(
		  status().isCreated());
		 

		mockMvc.perform(
				get("/items/search/findByName?name={name}", "Item 1")).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.items[0].name").value(
										"Item 1"));
	}
	
	
	@Test
	public void shouldQueryItemDescription() throws Exception {
		
		objItem.setName("Item 200");
		objItem.setDescription("For testing Item Respository using Factories");
		String json = mapper.writeValueAsString(objItem);
		mockMvc.perform(post("/items")
				  .content(json)).andExpect(
						  status().isCreated());
		mockMvc.perform(
				get("/items/search/findByDescription?description={description}", "For testing Item Respository using Factories")).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.items[0].description").value(
										"For testing Item Respository using Factories"));
	}

	
	@Test
	public void shouldQueryItemCreatedBy() throws Exception {
		
		objItem.setName("Item 200");
		objItem.setDescription("For testing Item Respository using Factories");
		objItem.setCreatedBy("Developer");
		String json = mapper.writeValueAsString(objItem);
		mockMvc.perform(post("/items")
				  .content(json)).andExpect(
						  status().isCreated());
		mockMvc.perform(
				get("/items/search/findByCreatedBy?createdBy={createdBy}", "Developer")).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.items[0].createdBy").value(
										"Developer"));
	}
	
	/*
	 * @Test public void shouldQueryItemCreatedDate() throws Exception {
	 * 
	 * objItem.setName("Item 200");
	 * objItem.setDescription("For testing Item Respository using Factories");
	 * objItem.setCreatedDate(1012019); String json =
	 * mapper.writeValueAsString(objItem); mockMvc.perform(post("/items")
	 * .content(json)).andExpect( status().isCreated()); mockMvc.perform(
	 * get("/items/search/findByCreatedDate?createdDate={createdDate}",
	 * "1012019")).andDo( MockMvcResultHandlers.print()).andExpect(
	 * status().isOk()).andExpect(
	 * jsonPath("$._embedded.items[0].createdDate").value( "1012019")); }
	 */

	
	
	@Test
	public void shouldUpdateItem() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/items").content(
				"{\"name\": \"Item 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(put(location).content(
				"{\"name\": \"Item 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Item 1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldPartiallyUpdateItem() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/items").content(
				"{\"name\": \"Item 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(
				patch(location).content("{\"name\": \"Item 1.1.1\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Item 1.1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldDeleteItem() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/items").content(
				"{ \"name\": \"Item 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(delete(location)).andExpect(status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isNotFound());
	}
}
