package edu.indiana.dlib.amppd.service;



import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.BatchValidationService;
import edu.indiana.dlib.amppd.web.ValidationResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ManifestValidationServiceTests {
	@Autowired
	private MockMvc mockMvc;
	

	@Autowired
    private BatchValidationService manifestService;
	@Autowired
    private CollectionRepository collectionRepository;
	@Autowired
    private UnitRepository unitRepository;

	@Before
	public void createTestData() throws Exception {

		collectionRepository.deleteAll();
		
		unitRepository.deleteAll();
		
		mockMvc.perform(post("/units").content(
				"{\"name\": \"Test Unit\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andExpect(
								header().string("Location", containsString("units/")));

		mockMvc.perform(post("/collections").content(
				"{ \"name\": \"Music Library\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();
		
		

	}
	
	@Test
	public void shouldBeValid() throws Exception {
		String fileName = "batch_manifest_for_testing.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        
        System.out.println(content);
        
        ValidationResponse response = manifestService.validate("Test Unit", content);
        
        Assert.assertFalse(response.hasErrors());
	}
	
	@Test
	public void shouldBeInvalidUnitName() throws Exception {
		String fileName = "batch_manifest_for_testing.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        
        System.out.println(content);
                
        ValidationResponse response = manifestService.validate("Invalid Unit", content);
        
        Assert.assertTrue(response.hasErrors());
	}
	
	@Test
	public void shouldBeInvalidFile() throws Exception {
		String fileName = "batch_manifest_blank.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        
        System.out.println(content);
                
        ValidationResponse response = manifestService.validate("Test Unit", content);
        
        Assert.assertTrue(response.hasErrors());
	}

	@Test
	public void shouldBeInvalidPrimaryFile() throws Exception {
		String fileName = "batch_manifest_invalid_primary_file.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
                        
        ValidationResponse response = manifestService.validate("Test Unit", content);
        
        Assert.assertTrue(response.hasErrors());
	}
	@Test
	public void shouldBeInvalidPrimaryFileDuplicate() throws Exception {
		String fileName = "batch_manifest_duplicate_primary.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
                        
        ValidationResponse response = manifestService.validate("Test Unit", content);
        
        Assert.assertTrue(response.hasErrors());
	}
	@Test
	public void shouldBeInvalidSupplementFileDuplicate() throws Exception {
		String fileName = "batch_manifest_duplicate_supplement.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
                        
        ValidationResponse response = manifestService.validate("Test Unit", content);
        
        Assert.assertTrue(response.hasErrors());
	}
	@Test
	public void shouldBeInvalidPrimaryFileWithPrimarySupplement() throws Exception {
		String fileName = "batch_manifest_invalid_primary_file_2.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
                        
        ValidationResponse response = manifestService.validate("Test Unit", content);
        
        Assert.assertTrue(response.hasErrors());
	}
	@Test
	public void shouldBeInvalidSuppFile() throws Exception {
		String fileName = "batch_manifest_invalid_supp_file.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
                        
        ValidationResponse response = manifestService.validate("Test Unit", content);
        
        Assert.assertTrue(response.hasErrors());
	}
}


