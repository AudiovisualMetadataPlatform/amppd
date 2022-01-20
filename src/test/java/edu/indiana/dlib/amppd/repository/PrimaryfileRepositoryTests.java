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
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.util.TestUtil;

@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PrimaryfileRepositoryTests {
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
	public static void setup() {
	    FixtureFactoryLoader.loadTemplates("edu.indiana.dlib.amppd.fixture");
	}
	
	@Before
	public void deleteAllBeforeTests() throws Exception {
		// TODO do a more refined delete to remove all data that might cause conflicts for tests in this class 
		// deleting all as below causes SQL FK violation when running the whole test suites, even though running this test class alone is fine,
		// probably due to the fact that some other tests call TestHelper to create the complete hierarchy of data entities from unit down to primaryfile
//		primaryfileRepository.deleteAll();
		token = "Bearer " + testHelper.getToken();
	}

	@Test
	public void shouldCreatePrimaryfile() throws Exception {
		// get a valid random primaryfile fixture
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfile);
		
//		// mock primaryfile json and mediaFilebinary as MultipartFiles	
//		MockMultipartFile jsonfile = new MockMultipartFile("primaryfile", null, "application/json", json.getBytes());		
//      MockMultipartFile mediaFile = new MockMultipartFile("mediaFile", "testprimaryfile.mp4", "text/plain", "Fake content for test primaryfile".getBytes());
//      primaryfile.setMediaFile(mediaFile);

		// create the primaryfile, should succeed with the primaryfile's URL as the location header
//		mockMvc.perform(multipart("/primaryfiles")
//			.file(jsonfile).file(mediaFile).header("Authorization", token))
		mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("primaryfiles/")));
	}
	
	@Test
	public void shouldListPrimaryfiles() throws Exception {
		// create an primaryfile to ensure some primaryfiles exist for listing 
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfile);
		mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json)).andReturn();		

		// list all primaryfiles, should include at least one primaryfile
		mockMvc.perform(get("/primaryfiles").header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.primaryfiles").exists())
			.andExpect(jsonPath("$._embedded.primaryfiles").isNotEmpty());	
	}	

	@Test
	public void shouldRetrievePrimaryfile() throws Exception {
		// create an primaryfile for retrieval
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfile);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		// retrieve the created primaryfile by accessing the returned location, fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(primaryfile.getName()))
			.andExpect(jsonPath("$.description").value(primaryfile.getDescription()));	
	}

	@Test
	public void shouldQueryPrimaryfiles() throws Exception {
		// create an primaryfile to ensure some primaryfiles exist for querying 
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfile);
		mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json)).andReturn();

		// query primaryfiles by name, should return at least one primaryfile
		mockMvc.perform(get("/primaryfiles/search/findByName?name={name}", primaryfile.getName()).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.primaryfiles[0].name").value(primaryfile.getName()));
	}

	@Test
	public void shouldUpdatePrimaryfile() throws Exception {
		// create an primaryfile for update
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfile);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update user-changeable fields
		primaryfile.setName(primaryfile.getName() + " Updated");
		primaryfile.setDescription(primaryfile.getDescription() + " updated");
		json = testUtil.toJson(primaryfile);

		// update the whole primaryfile
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated primaryfile, updated fields should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(primaryfile.getName()))
			.andExpect(jsonPath("$.description").value(primaryfile.getDescription()));
	}

	@Test
	public void shouldPartialUpdatePrimaryfile() throws Exception {
		// create an primaryfile for partial-update
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfile);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");
		
		// update only the name field
		String name = primaryfile.getName() + " Updated"; 
		json = "{\"name\": \"" + name + "\"}";

		// partial-update the primaryfile
		mockMvc.perform(patch(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		// retrieve the updated primaryfile, updated name should match
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(name));
	}

	@Test
	public void shouldDeletePrimaryfile() throws Exception {
		// create an primaryfile for delete
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfile);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json)).andReturn();			
		String location = mvcResult.getResponse().getHeader("Location");
		
		// delete the created primaryfile, then retrieve the same primaryfile, should return nothing
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
	
	@Test
	public void shouldErrorOnInvalidCreate() throws Exception {
		// get an invalid random primaryfile fixture
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).gimme("invalid");
		String json = testUtil.toJson(primaryfile);
		
		// create the invalid primaryfile, should fail with all validation errors
		mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(2)));
//			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.primaryfile.name"))
//			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
//			.andExpect(jsonPath("$.validationErrors[3].field").value("handleBeforeCreate.primaryfile.item"))
//			.andExpect(jsonPath("$.validationErrors[3].message").value("must not be null"));
	}
	
	@Test
	public void shouldErrorOnDuplicateCreate() throws Exception {
		// create a valid primaryfile 1st time
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfile);
		mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json)).andReturn();		
		
		// create the above primaryfile 2nd time, should fail with non-unique name validation error
		mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeCreate.primaryfile"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("primaryfile name must be unique within its parent item"));
	}
		
	@Test
	public void shouldErrorOnInvalidUpdate() throws Exception {
		// create a valid primaryfile for update
		Primaryfile primaryfile = Fixture.from(Primaryfile.class).uses(dataentityProcessor).gimme("valid");
		String json = testUtil.toJson(primaryfile);
		MvcResult mvcResult = mockMvc.perform(post("/primaryfiles").header("Authorization", token).content(json)).andReturn();	
		String location = mvcResult.getResponse().getHeader("Location");		
		
		// update user-changeable fields to invalid values
		primaryfile.setName("");
		json = testUtil.toJson(primaryfile);
		
		// update the primaryfile with invalid fields, should fail with all validation errors
		mockMvc.perform(put(location).header("Authorization", token).content(json))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.validationErrors").isArray())
			.andExpect(jsonPath("$.validationErrors", hasSize(1)))
			.andExpect(jsonPath("$.validationErrors[0].field").value("handleBeforeUpdate.primaryfile.name"))
			.andExpect(jsonPath("$.validationErrors[0].message").value("must not be blank"));
	}
	
}
