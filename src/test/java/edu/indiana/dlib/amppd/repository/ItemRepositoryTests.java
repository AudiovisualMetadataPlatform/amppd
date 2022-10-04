package edu.indiana.dlib.amppd.repository;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import edu.indiana.dlib.amppd.fixture.DataentityProcessor;
import edu.indiana.dlib.amppd.model.Item;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.util.TestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ItemRepositoryTests {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired 
	private TestHelper testHelper;
	
	@Autowired
    private TestUtil testUtil;	

	@Autowired 
	private DataentityProcessor dataentityProcessor;
	
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
//		itemRepository.deleteAll();
		token = "Bearer " + testHelper.getToken();
	}

	@Test
	public void shouldCreateItem() throws Exception {
		// get a valid random item fixture
		Item item = Fixture.from(Item.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(item);
		
		// create the item, should succeed with the item's URL as the location header
		mockMvc.perform(post("/items").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("items/")));
	}
	
	@Test
	public void shouldListItems() throws Exception {
		// create an item to ensure some items exist for listing 
		Item item = Fixture.from(Item.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(item);
		mockMvc.perform(post("/items").header("Authorization", token).content(json)).andReturn();		

		// list all items, should include at least one item
		mockMvc.perform(get("/items").header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.items").exists())
			.andExpect(jsonPath("$._embedded.items").isNotEmpty());	
	}	

	@Test
	public void shouldRetrieveItem() throws Exception {
		// create an item for retrieval
		Item item = Fixture.from(Item.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(item);
		MvcResult mvcResult = mockMvc.perform(post("/items").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		// retrieve the created item by accessing the returned location, fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(item.getName()))
			.andExpect(jsonPath("$.description").value(item.getDescription()));	
	}

	@Test
	public void shouldQueryItems() throws Exception {
		// create an item to ensure some items exist for querying 
		Item item = Fixture.from(Item.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(item);
		mockMvc.perform(post("/items").header("Authorization", token).content(json)).andReturn();

		// query items by name, should return at least one item
		mockMvc.perform(get("/items/search/findByName?name={name}", item.getName()).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.items[0].name").value(item.getName()));
	}

	@Test
	public void shouldUpdateItem() throws Exception {
		// create an item for update
		Item item = Fixture.from(Item.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(item);
		MvcResult mvcResult = mockMvc.perform(post("/items").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update user-changeable fields
		item.setName(item.getName() + " Updated");
		item.setDescription(item.getDescription() + " updated");
		json = testUtil.toJson(item);

		// update the whole item
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated item, updated fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(item.getName()))
			.andExpect(jsonPath("$.description").value(item.getDescription()));
	}

	@Test
	public void shouldPartialUpdateItem() throws Exception {
		// create an item for partial-update
		Item item = Fixture.from(Item.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(item);
		MvcResult mvcResult = mockMvc.perform(post("/items").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update only the name field
		String name = item.getName() + " Updated"; 
		json = "{\"name\": \"" + name + "\"}";

		// partial-update the item
		mockMvc.perform(patch(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated item, updated name should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(name));
	}

	@Test
	public void shouldDeleteItem() throws Exception {
		// create an item for delete
		Item item = Fixture.from(Item.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(item);
		MvcResult mvcResult = mockMvc.perform(post("/items").header("Authorization", token).content(json)).andReturn();			
		String location = mvcResult.getResponse().getHeader("Location");
		
		// delete the created item, then retrieve the same item, should return nothing
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
	
	@Test
	public void shouldErrorOnInvalidCreate() throws Exception {
		// get an invalid random item fixture
		Item item = Fixture.from(Item.class).gimme("invalid");
		String json = testUtil.toJson(item);
		
		// create the invalid item, should fail with all validation errors
		mockMvc.perform(post("/items").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(4)));
//			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.item.name"))
//			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"))
//			.andExpect(jsonPath("$.validationErrors[2].field").value("handleBeforeCreate.item.externalSource"))
//			.andExpect(jsonPath("$.validationErrors[2].message").value("must be one of the enumerated values defined in configuration property"))
//			.andExpect(jsonPath("$.validationErrors[3].field").value("handleBeforeCreate.item.collection"))
//			.andExpect(jsonPath("$.validationErrors[3].message").value("must not be null"));
	}
	
	@Test
	public void shouldErrorOnDuplicateCreate() throws Exception {
		// create a valid item 1st time
		Item item = Fixture.from(Item.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(item);
		mockMvc.perform(post("/items").header("Authorization", token).content(json)).andReturn();		
		
		// create the above item 2nd time, should fail with non-unique name validation error
		mockMvc.perform(post("/items").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(2)));
//			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.item"))
//			.andExpect(jsonPath("$.validationErrors[0].message").value("item name must be unique within its parent collection"));
	}
		
	@Test
	public void shouldErrorOnInvalidUpdate() throws Exception {
		// create a valid item for update
		Item item = Fixture.from(Item.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(item);
		MvcResult mvcResult = mockMvc.perform(post("/items").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");		
		
		// update user-changeable fields to invalid values
		item.setName("");
		json = testUtil.toJson(item);
		
		// update the item with invalid fields, should fail with all validation errors
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeUpdate.item.name"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
	}

}

