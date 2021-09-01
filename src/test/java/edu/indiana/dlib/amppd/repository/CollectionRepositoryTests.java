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

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.six2six.fixturefactory.Fixture;
import br.com.six2six.fixturefactory.loader.FixtureFactoryLoader;
import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class CollectionRepositoryTests {
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
//		collectionRepository.deleteAll(); 		
		token = "Bearer " + testHelper.getToken();
	}

	@Test
	public void shouldReturnRepositoryIndex() throws Exception {
		mockMvc.perform(get("/").header("Authorization", token)).andDo(print())
		.andExpect(status().isOk())
		.andExpect(jsonPath("$._links.collections").exists());
	}

	@Test
	public void shouldCreateCollection() throws Exception {
		Collection collection = Fixture.from(Collection.class).gimme("valid");
		String json = mapper.writeValueAsString(collection);
		
		mockMvc.perform(post("/collections").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("collections/")));
	}
	
	@Test
	public void shouldCreateCollectionWithLongDescription() throws Exception {
		// generate long description beyond default VARCHAR max length
		StringBuffer description = new StringBuffer();
		for (int i=0; i<256; i++) {
			description.append("Long description ");
		}

		Collection collection = Fixture.from(Collection.class).gimme("valid");
		collection.setDescription(description.toString());
		String json = mapper.writeValueAsString(collection);
		
		mockMvc.perform(post("/collections").header("Authorization", token).content(json))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", containsString("collections/")));			
	}	
	
	@Test
	public void shouldListCollections() throws Exception {
		Collection collection = Fixture.from(Collection.class).gimme("valid");
		String json = mapper.writeValueAsString(collection);
		MvcResult mvcResult = mockMvc.perform(post("/collections").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$._embedded.collections").exists())
			.andExpect(jsonPath("$._embedded.collections").isNotEmpty());	
	}	
	
	@Test
	public void shouldRetrieveCollection() throws Exception {
		Collection collection = Fixture.from(Collection.class).gimme("valid");
		String json = mapper.writeValueAsString(collection);
		MvcResult mvcResult = mockMvc.perform(post("/collections").header("Authorization", token).content(json)).andReturn();		
		String location = mvcResult.getResponse().getHeader("Location");
		
		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(collection.getName()))
			.andExpect(jsonPath("$.description").value(collection.getDescription()));
	}

	@Test
	public void shouldQueryCollections() throws Exception {
		Collection collection = Fixture.from(Collection.class).gimme("valid");
		String json = mapper.writeValueAsString(collection);
		mockMvc.perform(post("/collections").header("Authorization", token).content(json)).andReturn();

		mockMvc.perform(get("/collections/search/findByName?name={name}", collection.getName()).header("Authorization", token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$._embedded.collections[0].name").value(collection.getName()));
	}

	@Test
	public void shouldUpdateCollection() throws Exception {
		Collection collection = Fixture.from(Collection.class).gimme("valid");
		String json = mapper.writeValueAsString(collection);
		MvcResult mvcResult = mockMvc.perform(post("/collections").header("Authorization", token).content(json)).andReturn();	
		
		String location = mvcResult.getResponse().getHeader("Location");
		collection.setName(collection.getName() + " Updated");
		collection.setDescription(collection.getDescription() + " updated");
		json = mapper.writeValueAsString(collection);

		mockMvc.perform(put(location).header("Authorization", token).content(json))
				.andExpect(status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", token))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value(collection.getName()))
			.andExpect(jsonPath("$.description").value(collection.getDescription()));
	}

	@Test
	public void shouldPartiallyUpdateCollection() throws Exception {
		Collection collection = Fixture.from(Collection.class).gimme("valid");
		String json = mapper.writeValueAsString(collection);
		MvcResult mvcResult = mockMvc.perform(post("/collections").header("Authorization", token).content(json)).andReturn();	
		
		String location = mvcResult.getResponse().getHeader("Location");
		String name = collection.getName() + " Updated";
		json = "{\"name\": " + name + "}";

		mockMvc.perform(patch(location).header("Authorization", token).content(json))
			.andExpect(status().isNoContent());

		mockMvc.perform(get(location).header("Authorization", token))
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.name").value(name));
	}

	@Test
	public void shouldDeleteCollection() throws Exception {
		Collection collection = Fixture.from(Collection.class).gimme("valid");
		String json = mapper.writeValueAsString(collection);
		MvcResult mvcResult = mockMvc.perform(post("/collections").header("Authorization", token).content(json)).andReturn();			
		String location = mvcResult.getResponse().getHeader("Location");
		
		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
	}
	
//	@Test
//	public void shouldUpdateCollection() throws Exception {
//		MvcResult mvcResult = mockMvc
//				.perform(post("/collections").header("Authorization", token).content("{\"name\": \"Collection 1\", \"description\":\"For test\"}"))
//				.andExpect(status().isCreated()).andReturn();
//
//		String location = mvcResult.getResponse().getHeader("Location");
//
//		mockMvc.perform(put(location).header("Authorization", token).content("{\"name\": \"Collection 1.1\", \"description\":\"For test\"}"))
//				.andExpect(status().isNoContent());
//
//		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isOk()).andExpect(jsonPath("$.name").value("Collection 1.1"))
//				.andExpect(jsonPath("$.description").value("For test"));
//	}
//
//	@Test
//	public void shouldPartiallyUpdateCollection() throws Exception {
//		MvcResult mvcResult = mockMvc
//				.perform(post("/collections").header("Authorization", token).content("{\"name\": \"Collection 1\", \"description\":\"For test\"}"))
//				.andExpect(status().isCreated()).andReturn();
//
//		String location = mvcResult.getResponse().getHeader("Location");
//
//		mockMvc.perform(patch(location).header("Authorization", token).content("{\"name\": \"Collection 1.1.1\"}")).andExpect(status().isNoContent());
//
//		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isOk())
//				.andExpect(jsonPath("$.name").value("Collection 1.1.1"))
//				.andExpect(jsonPath("$.description").value("For test"));
//	}
//
//	@Test
//	public void shouldDeleteCollection() throws Exception {
//		MvcResult mvcResult = mockMvc
//				.perform(post("/collections").header("Authorization", token).content("{ \"name\": \"Collection 1.1\", \"description\":\"For test\"}"))
//				.andExpect(status().isCreated()).andReturn();
//
//		String location = mvcResult.getResponse().getHeader("Location");
//		mockMvc.perform(delete(location).header("Authorization", token)).andExpect(status().isNoContent());
//
//		mockMvc.perform(get(location).header("Authorization", token)).andExpect(status().isNotFound());
//	}

}
