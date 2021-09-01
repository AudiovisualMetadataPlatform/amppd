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
import edu.indiana.dlib.amppd.fixture.DataentityProcessor;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UnitRepositoryTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
    private TestHelper testHelper;

	@Autowired 
	private DataentityProcessor dataentityProcessor;

	@Autowired 
	private ObjectMapper mapper;
	
	private String token;
		
	@BeforeClass
	public static void loadFixture() {
		FixtureFactoryLoader.loadTemplates("edu.indiana.dlib.amppd.fixture");
	}
	
	@Before
	public void setup() throws Exception {
		// TODO do a more refined delete to remove all data that might cause conflicts for tests in this class 
		// deleting all as below causes SQL FK violation when running the whole test suites, even though running this test class alone is fine,
		// probably due to the fact that some other tests call TestHelper to create the complete hierarchy of data entities from unit down to primaryfile
//		unitRepository.deleteAll();
		token = "Bearer " + testHelper.getToken();
	}

	@Test
	public void shouldReturnRepositoryIndex() throws Exception {
		// the root URL should include a link for units
		mockMvc.perform(get("/").header("Authorization", token)).andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._links.units").exists());
	}

	@Test
	public void shouldCreateUnit() throws Exception {
		// get a valid random unit fixture
		Unit unit = Fixture.from(Unit.class).uses(dataentityProcessor).uses(dataentityProcessor).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		
		// create the unit, should succeed with the unit's URL as the location header
		mockMvc.perform(post("/units").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("units/")));
	}
	
	@Test
	public void shouldListUnits() throws Exception {
		// create a unit to ensure some units exist for listing 
		Unit unit = Fixture.from(Unit.class).uses(dataentityProcessor).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();		

		// list all units, should include at least one unit
		mockMvc.perform(get("/units").header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.units").exists())
			.andExpect(jsonPath("$._embedded.units").isNotEmpty());	
	}	

	@Test
	public void shouldRetrieveUnit() throws Exception {
		// create a unit for retrieval
		Unit unit = Fixture.from(Unit.class).uses(dataentityProcessor).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		MvcResult mvcResult = mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		// retrieve the created unit by accessing the returned location, fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(unit.getName()))
			.andExpect(jsonPath("$.description").value(unit.getDescription()));	
	}

	@Test
	public void shouldQueryUnits() throws Exception {
		// create a unit to ensure some units exist for querying 
		Unit unit = Fixture.from(Unit.class).uses(dataentityProcessor).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();

		// query units by name, should return at least one unit
		mockMvc.perform(get("/units/search/findByName?name={name}", unit.getName()).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.units[0].name").value(unit.getName()));
	}

	@Test
	public void shouldUpdateUnit() throws Exception {
		// create a unit for update
		Unit unit = Fixture.from(Unit.class).uses(dataentityProcessor).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		MvcResult mvcResult = mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();	
		
		// update user-changeable fields
		String location = mvcResult.getResponse().getHeader("Location");
		unit.setName(unit.getName() + " Updated");
		unit.setDescription(unit.getDescription() + " updated");
		json = mapper.writeValueAsString(unit);

		// update the whole unit
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated unit, updated fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(unit.getName()))
			.andExpect(jsonPath("$.description").value(unit.getDescription()));
	}

	@Test
	public void shouldPartiallyUpdateUnit() throws Exception {
		// create a unit for partial update
		Unit unit = Fixture.from(Unit.class).uses(dataentityProcessor).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		MvcResult mvcResult = mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();	
		
		// update only the name field
		String location = mvcResult.getResponse().getHeader("Location");
		String name = unit.getName() + " Updated"; 
		json = "{\"name\": \"" + name + "\"}";

		// update just the name field of the unit
		mockMvc.perform(patch(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated unit, updated name should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(name));
	}

	@Test
	public void shouldDeleteUnit() throws Exception {
		// create a unit for delete
		Unit unit = Fixture.from(Unit.class).uses(dataentityProcessor).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		MvcResult mvcResult = mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();			
		String location = mvcResult.getResponse().getHeader("Location");
		
		// delete the created unit, then retrieve the same unit, should return nothing
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
	
	@Test
	public void shouldErrorOnInvalidCreate() throws Exception {
		// get an invalid random unit fixture
		Unit unit = Fixture.from(Unit.class).uses(dataentityProcessor).uses(dataentityProcessor).gimme("invalid");
		String json = mapper.writeValueAsString(unit);
		
		// create the invalid unit, should fail with all validation errors
		mockMvc.perform(post("/units").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isNotEmpty())
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.unit.name"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
	}
	
	@Test
	public void shouldErrorOnDuplicateCreate() throws Exception {
		// create a valid unit 1st time
		Unit unit = Fixture.from(Unit.class).uses(dataentityProcessor).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();		
		
		// create the above unit 2nd time, should fail with non-unique name validation error
		mockMvc.perform(post("/units").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isNotEmpty())
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.unit"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("dataentity name must be unique within its parent's scope"));
	}
		
}
