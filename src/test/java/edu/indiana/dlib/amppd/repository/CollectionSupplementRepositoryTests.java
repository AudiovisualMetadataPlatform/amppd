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
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.util.TestUtil;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CollectionSupplementRepositoryTests {
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
		// probably due to the fact that some other tests call TestHelper to create the complete hierarchy of data entities from unit down to collectionSupplement
//		collectionSupplementRepository.deleteAll();
		token = "Bearer " + testHelper.getToken();
	}

	@Test
	public void shouldCreateCollectionSupplement() throws Exception {
		// get a valid random collectionSupplement fixture
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(collectionSupplement);
		
		// create the collectionSupplement, should succeed with the collectionSupplement's URL as the location header
		mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("collectionSupplements/")));
	}
	
	@Test
	public void shouldListCollectionSupplements() throws Exception {
		// create an collectionSupplement to ensure some collectionSupplements exist for listing 
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(collectionSupplement);
		mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json)).andReturn();		

		// list all collectionSupplements, should include at least one collectionSupplement
		mockMvc.perform(get("/collectionSupplements").header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.collectionSupplements").exists())
			.andExpect(jsonPath("$._embedded.collectionSupplements").isNotEmpty());	
	}	

	@Test
	public void shouldRetrieveCollectionSupplement() throws Exception {
		// create an collectionSupplement for retrieval
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(collectionSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		// retrieve the created collectionSupplement by accessing the returned location, fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(collectionSupplement.getName()))
			.andExpect(jsonPath("$.description").value(collectionSupplement.getDescription()));	
	}

	@Test
	public void shouldQueryCollectionSupplements() throws Exception {
		// create an collectionSupplement to ensure some collectionSupplements exist for querying 
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(collectionSupplement);
		mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json)).andReturn();

		// query collectionSupplements by name, should return at least one collectionSupplement
		mockMvc.perform(get("/collectionSupplements/search/findByName?name={name}", collectionSupplement.getName()).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.collectionSupplements[0].name").value(collectionSupplement.getName()));
	}

	@Test
	public void shouldUpdateCollectionSupplement() throws Exception {
		// create an collectionSupplement for update
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(collectionSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update user-changeable fields
		collectionSupplement.setName(collectionSupplement.getName() + " Updated");
		collectionSupplement.setDescription(collectionSupplement.getDescription() + " updated");
		json = testUtil.toJson(collectionSupplement);

		// update the whole collectionSupplement
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated collectionSupplement, updated fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(collectionSupplement.getName()))
			.andExpect(jsonPath("$.description").value(collectionSupplement.getDescription()));
	}

	@Test
	public void shouldPartialUpdateCollectionSupplement() throws Exception {
		// create an collectionSupplement for partial-update
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(collectionSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update only the name field
		String name = collectionSupplement.getName() + " Updated"; 
		json = "{\"name\": \"" + name + "\"}";

		// partial-update the collectionSupplement
		mockMvc.perform(patch(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated collectionSupplement, updated name should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(name));
	}

	@Test
	public void shouldDeleteCollectionSupplement() throws Exception {
		// create an collectionSupplement for delete
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(collectionSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json)).andReturn();			
		String location = mvcResult.getResponse().getHeader("Location");
		
		// delete the created collectionSupplement, then retrieve the same collectionSupplement, should return nothing
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
	
	@Test
	public void shouldErrorOnInvalidCreate() throws Exception {
		// get an invalid random collectionSupplement fixture
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).gimme("invalid");
		String json = testUtil.toJson(collectionSupplement);
		
		// create the invalid collectionSupplement, should fail with all validation errors
		mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(2)));
//			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.collectionSupplement.name"))
//			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
//			.andExpect(jsonPath("$.validationErrors[3].field").value("handleBeforeCreate.collectionSupplement.item"))
//			.andExpect(jsonPath("$.validationErrors[3].message").value("must not be null"));
	}
	
	@Test
	public void shouldErrorOnDuplicateCreate() throws Exception {
		// create a valid collectionSupplement 1st time
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(collectionSupplement);
		mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json)).andReturn();		
		
		// create the above collectionSupplement 2nd time, should fail with non-unique name validation error
		mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.supplement"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("collectionSupplement name must be unique within its parent collection"));
	}
		
	@Test
	public void shouldErrorOnInvalidUpdate() throws Exception {
		// create a valid collectionSupplement for update
		CollectionSupplement collectionSupplement = Fixture.from(CollectionSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(collectionSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/collectionSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");		
		
		// update user-changeable fields to invalid values
		collectionSupplement.setName("");
		json = testUtil.toJson(collectionSupplement);
		
		// update the collectionSupplement with invalid fields, should fail with all validation errors
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeUpdate.supplement.name"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
	}

}
