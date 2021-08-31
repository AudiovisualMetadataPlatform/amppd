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
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PrimaryfileRepositoryTests {
	@Autowired
	private MockMvc mockMvc;

//	@Autowired
//	AmpUserService ampUserservice;
	
	@Autowired 
	private ObjectMapper mapper;
	private Primaryfile primaryfile ;

	@Autowired
    private TestHelper testHelper;
	String token = "";
	
	@BeforeClass
	public static void setupTest() 
	{
	    FixtureFactoryLoader.loadTemplates("edu.indiana.dlib.amppd.data");
	}
	
	@Before
	public void deleteAllBeforeTests() throws Exception {
		// TODO do a more refined delete to remove all data that might cause conflicts for tests in this class 
		// deleting all as below causes SQL FK violation when running the whole test suites, even though running this test class alone is fine,
		// probably due to the fact that some other tests call TestHelper to create the complete hierarchy of data entities from unit down to primaryfile
//		primaryfileRepository.deleteAll();
		token = testHelper.getToken();
	}

	@Test
	public void shouldReturnRepositoryIndex() throws Exception {

		mockMvc.perform(get("/").header("Authorization", "Bearer " + token)).andDo(print()).andExpect(status().isOk()).andExpect(
				jsonPath("$._links.primaryfiles").exists());
	}

	@Test
	public void shouldCreatePrimaryfile() throws Exception {
		mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"Primaryfile 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andExpect(
								header().string("Location", containsString("primaryfiles/")));
	}

	@Test
	public void shouldRetrievePrimaryfile() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"Primaryfile 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Primaryfile 1")).andExpect(
						jsonPath("$.description").value("For test"));
	}
	
	
	@Test
	public void shouldQueryPrimaryfileDescription() throws Exception {
		
		primaryfile = Fixture.from(Primaryfile.class).gimme("valid");
		
		String json = mapper.writeValueAsString(primaryfile);
		mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		mockMvc.perform(
				get("/primaryfiles/search/findByDescription?description={description}", primaryfile.getDescription()).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.primaryfiles[0].description").value(
										primaryfile.getDescription()));
	}

	
	/*
	 * @Test public void shouldQueryPrimaryfileCreatedDate() throws Exception {
	 * 
	 * objPrimaryFile.setName("Primary File 200"); objPrimaryFile.
	 * setDescription("For testing Primary File Respository using Factories");
	 * objPrimaryFile.setCreatedDate(1012019); String json =
	 * mapper.writeValueAsString(objPrimaryFile);
	 * mockMvc.perform(post("/primaryfiles") .content(json)).andExpect(
	 * status().isCreated()); mockMvc.perform(
	 * get("/primaryfiles/search/findByCreatedDate?createdDate={createdDate}",
	 * "1012019")).andDo( MockMvcResultHandlers.print()).andExpect(
	 * status().isOk()).andExpect(
	 * jsonPath("$._embedded.primaryfiles[0].createdDate").value( "1012019")); }
	 */

	
	@Test
	public void shouldQueryPrimaryfileCreatedBy() throws Exception {
		
		primaryfile = Fixture.from(Primaryfile.class).gimme("valid");
		
		String json = mapper.writeValueAsString(primaryfile);
		mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		
//		String username = ampUserservice.getCurrentUsername();
		mockMvc.perform(
				get("/primaryfiles/search/findByCreatedBy?createdBy={createdBy}", TestHelper.TEST_USER).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.primaryfiles[0].createdBy").value(
										TestHelper.TEST_USER));
	}
	

	@Test
	public void shouldQueryPrimaryfileName() throws Exception {

		mockMvc.perform(post("/primaryfiles").content(
				"{ \"name\": \"Primaryfile 1\", \"description\":\"For test\"}").header("Authorization", "Bearer " + token)).andExpect(
						status().isCreated());

		mockMvc.perform(
				get("/primaryfiles/search/findByName?name={name}", "Primaryfile 1").header("Authorization", "Bearer " + token)).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.primaryfiles[0].name").value(
										"Primaryfile 1"));
	}

	@Test
	public void shouldQueryPrimaryfileNameKeyword() throws Exception {
		
		primaryfile = Fixture.from(Primaryfile.class).gimme("valid");
		String[] words = StringUtils.split(primaryfile.getName());
		String keyword = words[words.length-1];
		
		String json = mapper.writeValueAsString(primaryfile);
		mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		
		mockMvc.perform(
				get("/primaryfiles/search/findByKeyword?keyword={keyword}", keyword).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.primaryfiles[0].name").value(new StringContains(keyword)));
	}

	@Test
	public void shouldQueryPrimaryfileNameKeywordCaseInsensitive() throws Exception {
		
		primaryfile = Fixture.from(Primaryfile.class).gimme("valid");
		String[] words = StringUtils.split(primaryfile.getName());
		String keyword = words[words.length-1].toUpperCase();
		
		String json = mapper.writeValueAsString(primaryfile);
		mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		
		mockMvc.perform(
				get("/primaryfiles/search/findByKeyword?keyword={keyword}", keyword).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.primaryfiles[0].name").value(new StringContains(keyword)));
		
		keyword = words[words.length-1].toLowerCase();

		mockMvc.perform(
				get("/primaryfiles/search/findByKeyword?keyword={keyword}", keyword).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.primaryfiles[0].name").value(new StringContains(keyword)));
	}
	@Test
	public void shouldQueryPrimaryfileDescriptionKeyword() throws Exception {
		
		primaryfile = Fixture.from(Primaryfile.class).gimme("valid");
		String[] words = StringUtils.split(primaryfile.getDescription());
		String keyword = words[words.length-1];

		String json = mapper.writeValueAsString(primaryfile);
		mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token)
				  .content(json)).andExpect(
						  status().isCreated());
		
		mockMvc.perform(
				get("/primaryfiles/search/findByKeyword?keyword={keyword}", keyword).header("Authorization", "Bearer " + token)).andDo(
						MockMvcResultHandlers.print()).andExpect(
						status().isOk()).andExpect(
								jsonPath("$._embedded.primaryfiles[0].description").value(new StringContains(keyword)));
	}	
	
	@Test
	public void shouldUpdatePrimaryfile() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"Primaryfile 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(put(location).header("Authorization", "Bearer " + token).content(
				"{\"name\": \"Primaryfile 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Primaryfile 1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldPartiallyUpdatePrimaryfile() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token).content(
				"{\"name\": \"Primaryfile 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(
				patch(location).header("Authorization", "Bearer " + token).content("{\"name\": \"Primaryfile 1.1.1\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Primaryfile 1.1.1")).andExpect(
						jsonPath("$.description").value("For test"));
	}

	@Test
	public void shouldDeletePrimaryfile() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/primaryfiles").header("Authorization", "Bearer " + token).content(
				"{ \"name\": \"Primaryfile 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(delete(location).header("Authorization", "Bearer " + token)).andExpect(status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
	}
}
