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
		mockMvc.perform(get("/").header("Authorization", token)).andDo(print())
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._links.units").exists());
	}

	@Test
	public void shouldCreateUnit() throws Exception {
		Unit unit = Fixture.from(Unit.class).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		
		mockMvc.perform(post("/units").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("units/")));
//			.andDo(MockMvcResultHandlers.print());	
		
//		mockMvc.perform(post("/units").header("Authorization", token).content(
//				"{\"name\": \"Unit 1\", \"description\":\"For test\"}")).andExpect(
//						status().isCreated()).andExpect(
//								header().string("Location", containsString("units/")));
	}
	
	@Test
	public void shouldListUnits() throws Exception {
		Unit unit = Fixture.from(Unit.class).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		MvcResult mvcResult = mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.units").exists())
			.andExpect(jsonPath("$._embedded.units").isNotEmpty());	
	}	

	@Test
	public void shouldRetrieveUnit() throws Exception {
		Unit unit = Fixture.from(Unit.class).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		MvcResult mvcResult = mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(unit.getName()))
			.andExpect(jsonPath("$.description").value(unit.getDescription()));
		
//		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
//				jsonPath("$.name").value("Unit 1")).andExpect(
//						jsonPath("$.description").value("For test"));		
	}

	@Test
	public void shouldQueryUnits() throws Exception {
		Unit unit = Fixture.from(Unit.class).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();

		mockMvc.perform(get("/units/search/findByName?name={name}", unit.getName()).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.units[0].name").value(unit.getName()));
		
//		mockMvc.perform(
//				get("/units/search/findByName?name={name}", "Unit 1").header("Authorization", token)).andExpect(
//						status().isOk()).andExpect(
//								jsonPath("$._embedded.units[0].name").value(
//										"Unit 1"));
	}

	@Test
	public void shouldUpdateUnit() throws Exception {
		Unit unit = Fixture.from(Unit.class).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		MvcResult mvcResult = mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();	
		
		String location = mvcResult.getResponse().getHeader("Location");
		unit.setName(unit.getName() + " Updated");
		unit.setDescription(unit.getDescription() + " updated");
		json = mapper.writeValueAsString(unit);

		mockMvc.perform(put(location).header("Authorization", token).content(json))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(unit.getName()))
			.andExpect(jsonPath("$.description").value(unit.getDescription()));
	}

	@Test
	public void shouldPartiallyUpdateUnit() throws Exception {
		Unit unit = Fixture.from(Unit.class).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		MvcResult mvcResult = mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();	
		
		String location = mvcResult.getResponse().getHeader("Location");
		String name = unit.getName() + " Updated";
		json = "{\"name\": " + name + "}";

		mockMvc.perform(patch(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", token))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value(name));
	}

	@Test
	public void shouldDeleteUnit() throws Exception {
		Unit unit = Fixture.from(Unit.class).gimme("valid");
		String json = mapper.writeValueAsString(unit);
		MvcResult mvcResult = mockMvc.perform(post("/units").header("Authorization", token).content(json)).andReturn();			
		String location = mvcResult.getResponse().getHeader("Location");
		
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
	
}
