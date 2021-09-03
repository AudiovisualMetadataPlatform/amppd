package edu.indiana.dlib.amppd.repository;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
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
import edu.indiana.dlib.amppd.fixture.DataentityProcessor;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.PrimaryfileSupplement;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.util.TestUtil;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PrimaryfileSupplementRepositoryTests {
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
//		primaryfileSupplementRepository.deleteAll();
		token = testHelper.getToken();
	}

	@Test
	public void shouldCreatePrimaryfileSupplement() throws Exception {
		// get a valid random primaryfileSupplement fixture
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfileSupplement);
		
		// create the primaryfileSupplement, should succeed with the primaryfileSupplement's URL as the location header
		mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("primaryfileSupplements/")));
	}
	
	@Test
	public void shouldListPrimaryfileSupplements() throws Exception {
		// create an primaryfileSupplement to ensure some primaryfileSupplements exist for listing 
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfileSupplement);
		mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json)).andReturn();		

		// list all primaryfileSupplements, should include at least one primaryfileSupplement
		mockMvc.perform(get("/primaryfileSupplements").header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.primaryfileSupplements").exists())
			.andExpect(jsonPath("$._embedded.primaryfileSupplements").isNotEmpty());	
	}	

	@Test
	public void shouldRetrievePrimaryfileSupplement() throws Exception {
		// create an primaryfileSupplement for retrieval
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfileSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		// retrieve the created primaryfileSupplement by accessing the returned location, fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(primaryfileSupplement.getName()))
			.andExpect(jsonPath("$.description").value(primaryfileSupplement.getDescription()));	
	}

	@Test
	public void shouldQueryPrimaryfileSupplements() throws Exception {
		// create an primaryfileSupplement to ensure some primaryfileSupplements exist for querying 
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfileSupplement);
		mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json)).andReturn();

		// query primaryfileSupplements by name, should return at least one primaryfileSupplement
		mockMvc.perform(get("/primaryfileSupplements/search/findByName?name={name}", primaryfileSupplement.getName()).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.primaryfileSupplements[0].name").value(primaryfileSupplement.getName()));
	}

	@Test
	public void shouldUpdatePrimaryfileSupplement() throws Exception {
		// create an primaryfileSupplement for update
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfileSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update user-changeable fields
		primaryfileSupplement.setName(primaryfileSupplement.getName() + " Updated");
		primaryfileSupplement.setDescription(primaryfileSupplement.getDescription() + " updated");
		json = testUtil.toJson(primaryfileSupplement);

		// update the whole primaryfileSupplement
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated primaryfileSupplement, updated fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(primaryfileSupplement.getName()))
			.andExpect(jsonPath("$.description").value(primaryfileSupplement.getDescription()));
	}

	@Test
	public void shouldPartialUpdatePrimaryfileSupplement() throws Exception {
		// create an primaryfileSupplement for partial-update
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfileSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update only the name field
		String name = primaryfileSupplement.getName() + " Updated"; 
		json = "{\"name\": \"" + name + "\"}";

		// partial-update the primaryfileSupplement
		mockMvc.perform(patch(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated primaryfileSupplement, updated name should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(name));
	}

	@Test
	public void shouldDeletePrimaryfileSupplement() throws Exception {
		// create an primaryfileSupplement for delete
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfileSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json)).andReturn();			
		String location = mvcResult.getResponse().getHeader("Location");
		
		// delete the created primaryfileSupplement, then retrieve the same primaryfileSupplement, should return nothing
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
	
	@Test
	public void shouldErrorOnInvalidCreate() throws Exception {
		// get an invalid random primaryfileSupplement fixture
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).gimme("invalid");
		String json = testUtil.toJson(primaryfileSupplement);
		
		// create the invalid primaryfileSupplement, should fail with all validation errors
		mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(2)));
//			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.primaryfileSupplement.name"))
//			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
//			.andExpect(jsonPath("$.validationErrors[3].field").value("handleBeforeCreate.primaryfileSupplement.primaryfile"))
//			.andExpect(jsonPath("$.validationErrors[3].message").value("must not be null"));
	}
	
	@Test
	public void shouldErrorOnDuplicateCreate() throws Exception {
		// create a valid primaryfileSupplement 1st time
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfileSupplement);
		mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json)).andReturn();		
		
		// create the above primaryfileSupplement 2nd time, should fail with non-unique name validation error
		mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.supplement"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("dataentity name must be unique within its parent's scope"));
	}
		
	@Test
	public void shouldErrorOnInvalidUpdate() throws Exception {
		// create a valid primaryfileSupplement for update
		PrimaryfileSupplement primaryfileSupplement = Fixture.from(PrimaryfileSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfileSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfileSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");		
		
		// update user-changeable fields to invalid values
		primaryfileSupplement.setName("");
		json = testUtil.toJson(primaryfileSupplement);
		
		// update the primaryfileSupplement with invalid fields, should fail with all validation errors
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeUpdate.supplement.name"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
	}
	
}
