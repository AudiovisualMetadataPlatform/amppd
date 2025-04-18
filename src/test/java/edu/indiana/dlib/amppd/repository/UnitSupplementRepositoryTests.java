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
import edu.indiana.dlib.amppd.model.UnitSupplement;
import edu.indiana.dlib.amppd.security.JwtTokenUtil;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.util.TestUtil;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UnitSupplementRepositoryTests {
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
		// probably due to the fact that some other tests call TestHelper to create the complete hierarchy of data entities from unit down to unitSupplement
//		unitSupplementRepository.deleteAll();
		token = JwtTokenUtil.JWT_AUTH_PREFIX + testHelper.getToken();
	}

	@Test
	public void shouldCreateUnitSupplement() throws Exception {
		// get a valid random unitSupplement fixture
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(unitSupplement);
		
		// create the unitSupplement, should succeed with the unitSupplement's URL as the location header
		mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("unitSupplements/")));
	}
	
	@Test
	public void shouldListUnitSupplements() throws Exception {
		// create an unitSupplement to ensure some unitSupplements exist for listing 
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(unitSupplement);
		mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json)).andReturn();		

		// list all unitSupplements, should include at least one unitSupplement
		mockMvc.perform(get("/unitSupplements").header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.unitSupplements").exists())
			.andExpect(jsonPath("$._embedded.unitSupplements").isNotEmpty());	
	}	

	@Test
	public void shouldRetrieveUnitSupplement() throws Exception {
		// create an unitSupplement for retrieval
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(unitSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		// retrieve the created unitSupplement by accessing the returned location, fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(unitSupplement.getName()))
			.andExpect(jsonPath("$.description").value(unitSupplement.getDescription()));	
	}

	@Test
	public void shouldQueryUnitSupplements() throws Exception {
		// create an unitSupplement to ensure some unitSupplements exist for querying 
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(unitSupplement);
		mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json)).andReturn();

		// query unitSupplements by name, should return at least one unitSupplement
		mockMvc.perform(get("/unitSupplements/search/findByName?name={name}", unitSupplement.getName()).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.unitSupplements[0].name").value(unitSupplement.getName()));
	}

	@Test
	public void shouldUpdateUnitSupplement() throws Exception {
		// create an unitSupplement for update
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(unitSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update user-changeable fields
		unitSupplement.setName(unitSupplement.getName() + " Updated");
		unitSupplement.setDescription(unitSupplement.getDescription() + " updated");
		json = testUtil.toJson(unitSupplement);

		// update the whole unitSupplement
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated unitSupplement, updated fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(unitSupplement.getName()))
			.andExpect(jsonPath("$.description").value(unitSupplement.getDescription()));
	}

	@Test
	public void shouldPartialUpdateUnitSupplement() throws Exception {
		// create an unitSupplement for partial-update
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(unitSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update only the name field
		String name = unitSupplement.getName() + " Updated"; 
		json = "{\"name\": \"" + name + "\"}";

		// partial-update the unitSupplement
		mockMvc.perform(patch(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated unitSupplement, updated name should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(name));
	}

	@Test
	public void shouldDeleteUnitSupplement() throws Exception {
		// create an unitSupplement for delete
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(unitSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json)).andReturn();			
		String location = mvcResult.getResponse().getHeader("Location");
		
		// delete the created unitSupplement, then retrieve the same unitSupplement, should return nothing
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
	
	@Test
	public void shouldErrorOnInvalidCreate() throws Exception {
		// get an invalid random unitSupplement fixture
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).gimme("invalid");
		String json = testUtil.toJson(unitSupplement);
		
		// create the invalid unitSupplement, should fail with all validation errors
		mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(4)));
//			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.unitSupplement.name"))
//			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
//			.andExpect(jsonPath("$.validationErrors[1].field").value("handleBeforeCreate.itemSupplement.category"))
//			.andExpect(jsonPath("$.validationErrors[1].message").value("must not be blank"));
//			.andExpect(jsonPath("$.validationErrors[2].field").value("handleBeforeCreate.unitSupplement.item"))
//			.andExpect(jsonPath("$.validationErrors[2].message").value("must not be null"));
	}
	
	@Test
	public void shouldErrorOnDuplicateCreate() throws Exception {
		// create a valid unitSupplement 1st time
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(unitSupplement);
		mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json)).andReturn();		
		
		// create the above unitSupplement 2nd time, should fail with non-unique name validation error
		mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.supplement"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("Unit supplement name must be unique within its parent unit"));
	}
		
	@Test
	public void shouldErrorOnInvalidUpdate() throws Exception {
		// create a valid unitSupplement for update
		UnitSupplement unitSupplement = Fixture.from(UnitSupplement.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(unitSupplement);
		MvcResult mvcResult = mockMvc.perform(post("/unitSupplements").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");		
		
		// update user-changeable fields to invalid values
		unitSupplement.setName("");
		json = testUtil.toJson(unitSupplement);
		
		// update the unitSupplement with invalid fields, should fail with all validation errors
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeUpdate.supplement.name"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
	}

}
