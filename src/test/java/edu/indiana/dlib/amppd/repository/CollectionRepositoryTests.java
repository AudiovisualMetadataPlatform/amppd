package edu.indiana.dlib.amppd.repository;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;

//import org.apache.catalina.mapper.Mapper;
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
import br.com.six2six.fixturefactory.Rule;
import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import edu.indiana.dlib.amppd.model.factory.ObjectFactory;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Dataentity;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CollectionRepositoryTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CollectionRepository collectionRepository;
	
	private ObjectMapper mapper = new ObjectMapper();
	private Collection obj;
	//private ObjectFactory objFactory = new ObjectFactory();
	
	
	@BeforeClass
	public static void setupTest() 
	{
	    FixtureFactoryLoader.loadTemplates("edu.indiana.dlib.amppd.testData");
	}
	
	/*
	 * @Before public void initiateBeforeTests() throws Exception { HashMap params =
	 * new HashMap<String, String>(); objCollection=
	 * (Collection)objFactory.createDataentityObject(params, "Collection");
	 * 
	 * }
	 */

	@Before 
	public void deleteAllBeforeTests() throws Exception {
		collectionRepository.deleteAll(); }


	@Test public void shouldReturnRepositoryIndex() throws Exception {
		mockMvc.perform(get("/")).andExpect(status().isOk()).
		andExpect( jsonPath("$._links.collections").exists()); }


	@Test public void shouldCreateCollection() throws Exception {
		mockMvc.perform(post("/collections").content(
				"{\"name\": \"Collection 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andDo(MockMvcResultHandlers.print()).andExpect(
								header().string("Location", containsString("collections/"))); }
	

	@Test public void shouldRetrieveCollection() throws Exception {
		
		
		mockMvc.perform(post("/collections").
				content("{\"name\": \"Collection 1\", \"description\":\"For test\"}"))
		.andExpect(status().isCreated()).andReturn(); }
	

	@Test public void shouldQueryCollection() throws Exception {
		obj = Fixture.from(Collection.class).gimme("valid");
		
		String json = mapper.writeValueAsString(obj);
		mockMvc.perform(post("/collections")
				  .content(json)).andExpect(
						  status().isCreated());
		
		mockMvc.perform(
				get("/collections/search/findByName?name={name}", obj.getName())).andDo(MockMvcResultHandlers.
						print()).andExpect( status().isOk()).andExpect(
								jsonPath("$._embedded.collections[0].name").value( obj.getName())) ; }
	

	@Test public void shouldUpdateCollection() throws Exception { 
	
		  MvcResult mvcResult = mockMvc.perform(post("/collections").content(
		  "{\"name\": \"Collection 1\", \"description\":\"For test\"}")).andExpect(
		  status().isCreated()).andReturn();
		 
		String location = mvcResult.getResponse().getHeader("Location");
		
		mockMvc.perform(put(location).content(
				"{\"name\": \"Collection 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isNoContent());
		 
		 mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
			jsonPath("$.name").value("Collection 1.1")).andExpect(
					jsonPath("$.description").value("For test")); }



	@Test public void shouldPartiallyUpdateCollection() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/collections").content(
				"{\"name\": \"Collection 1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");

		mockMvc.perform(
				patch(location).content("{\"name\": \"Collection 1.1.1\"}")).andExpect(
						status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isOk()).andExpect(
				jsonPath("$.name").value("Collection 1.1.1")).andExpect(
						jsonPath("$.description").value("For test")); }

	
	@Test public void shouldDeleteCollection() throws Exception {

		MvcResult mvcResult = mockMvc.perform(post("/collections").content(
				"{ \"name\": \"Collection 1.1\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();

		String location = mvcResult.getResponse().getHeader("Location");
		mockMvc.perform(delete(location)).andExpect(status().isNoContent());

		mockMvc.perform(get(location)).andExpect(status().isNotFound()); }


}
