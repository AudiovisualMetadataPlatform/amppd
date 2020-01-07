package edu.indiana.dlib.amppd.service;



import java.io.File;
import java.nio.file.Files;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.indiana.dlib.amppd.service.ManifestValidationService;
import edu.indiana.dlib.amppd.web.ValidationResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ManifestValidationServiceTests {
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired 
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
    private ManifestValidationService manifestService;

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


