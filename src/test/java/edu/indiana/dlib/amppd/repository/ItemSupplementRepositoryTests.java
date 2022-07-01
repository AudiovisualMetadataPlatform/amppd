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
import org.junit.Ignore;
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
import edu.indiana.dlib.amppd.model.ItemSupplement;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.util.TestUtil;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ItemSupplementRepositoryTests {
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
	public static void setupTest() 	{
	    FixtureFactoryLoader.loadTemplates("edu.indiana.dlib.amppd.fixture");
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
	public void shouldCreateItemSupplement() throws Exception {
		// get a valid random itemSupplement fixture
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(itemSupplement);
		
		// create the itemSupplement, should succeed with the itemSupplement's URL as the location header
		mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("itemSupplements/")));
	}
	
	@Test
	public void shouldListItemSupplements() throws Exception {
		// create an itemSupplement to ensure some itemSupplements exist for listing 
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(itemSupplement);
		mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json)).andReturn();		

		// list all itemSupplements, should include at least one itemSupplement
		mockMvc.perform(get("/itemSupplements").header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.itemSupplements").exists())
			.andExpect(jsonPath("$._embedded.itemSupplements").isNotEmpty());	
	}	

	@Test
	public void shouldRetrieveItemSupplement() throws Exception {
		// create an itemSupplement for retrieval
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(itemSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		// retrieve the created itemSupplement by accessing the returned location, fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(itemSupplement.getName()))
			.andExpect(jsonPath("$.description").value(itemSupplement.getDescription()));	
	}

	@Test
	public void shouldQueryItemSupplements() throws Exception {
		// create an itemSupplement to ensure some itemSupplements exist for querying 
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(itemSupplement);
		mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json)).andReturn();

		// query itemSupplements by name, should return at least one itemSupplement
		mockMvc.perform(get("/itemSupplements/search/findByName?name={name}", itemSupplement.getName()).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.itemSupplements[0].name").value(itemSupplement.getName()));
	}

	@Test
	public void shouldUpdateItemSupplement() throws Exception {
		// create an itemSupplement for update
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(itemSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update user-changeable fields
		itemSupplement.setName(itemSupplement.getName() + " Updated");
		itemSupplement.setDescription(itemSupplement.getDescription() + " updated");
		json = testUtil.toJson(itemSupplement);

		// update the whole itemSupplement
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated itemSupplement, updated fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(itemSupplement.getName()))
			.andExpect(jsonPath("$.description").value(itemSupplement.getDescription()));
	}

	@Test
	public void shouldPartialUpdateItemSupplement() throws Exception {
		// create an itemSupplement for partial-update
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(itemSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update only the name field
		String name = itemSupplement.getName() + " Updated"; 
		json = "{\"name\": \"" + name + "\"}";

		// partial-update the itemSupplement
		mockMvc.perform(patch(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated itemSupplement, updated name should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(name));
	}

	@Test
	public void shouldDeleteItemSupplement() throws Exception {
		// create an itemSupplement for delete
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(itemSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json)).andReturn();			
		String location = mvcResult.getResponse().getHeader("Location");
		
		// delete the created itemSupplement, then retrieve the same itemSupplement, should return nothing
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
	
	@Test
	public void shouldErrorOnInvalidCreate() throws Exception {
		// get an invalid random itemSupplement fixture
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).gimme("invalid");
		String json = testUtil.toJson(itemSupplement);
		
		// create the invalid itemSupplement, should fail with all validation errors
		mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(3)));
//			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.itemSupplement.name"))
//			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
//			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.itemSupplement.category"))
//			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
//			.andExpect(jsonPath("$.validationErrors[3].field").value("handleBeforeCreate.itemSupplement.item"))
//			.andExpect(jsonPath("$.validationErrors[3].message").value("must not be null"));
	}
	
	@Test
	public void shouldErrorOnDuplicateCreate() throws Exception {
		// create a valid itemSupplement 1st time
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(itemSupplement);
		mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json)).andReturn();		
		
		// create the above itemSupplement 2nd time, should fail with non-unique name validation error
		mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.supplement"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("itemSupplement name must be unique within its parent item"));
	}
		
	@Test
	public void shouldErrorOnInvalidUpdate() throws Exception {
		// create a valid itemSupplement for update
		ItemSupplement itemSupplement = Fixture.from(ItemSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(itemSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/itemSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");		
		
		// update user-changeable fields to invalid values
		itemSupplement.setName("");
		json = testUtil.toJson(itemSupplement);
		
		// update the itemSupplement with invalid fields, should fail with all validation errors
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeUpdate.supplement.name"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
	}

}
