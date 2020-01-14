package edu.indiana.dlib.amppd.service;



import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

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

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.repository.AmpUserRepository;
import edu.indiana.dlib.amppd.repository.BatchFileRepository;
import edu.indiana.dlib.amppd.repository.BatchRepository;
import edu.indiana.dlib.amppd.repository.BatchSupplementFileRepository;
import edu.indiana.dlib.amppd.repository.CollectionRepository;
import edu.indiana.dlib.amppd.repository.CollectionSupplementRepository;
import edu.indiana.dlib.amppd.repository.ItemRepository;
import edu.indiana.dlib.amppd.repository.ItemSupplementRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.repository.PrimaryfileSupplementRepository;
import edu.indiana.dlib.amppd.repository.SupplementRepository;
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.service.BatchValidationService;
import edu.indiana.dlib.amppd.web.ValidationResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class BatchServiceTests {
	@Autowired
	private MockMvc mockMvc;
	

	@Autowired
    private BatchService batchService;

	@Autowired
    private BatchRepository batchRepository;
	@Autowired
    private BatchFileRepository batchFileRepository;
	@Autowired
    private BatchSupplementFileRepository batchSupplementFileRepository;
	@Autowired
    private CollectionRepository collectionRepository;
	@Autowired
    private UnitRepository unitRepository;
	@Autowired
    private ItemRepository itemRepository;
	@Autowired
    private PrimaryfileRepository pfRepository;
	@Autowired
    private PrimaryfileSupplementRepository pfsupplementRepository;
	@Autowired
    private CollectionSupplementRepository csupplementRepository;
	@Autowired
    private ItemSupplementRepository isupplementRepository;
	@Autowired
    private BatchValidationService manifestService;
	@Autowired
    private AmpUserRepository ampUserRepository;

	@Before
	public void createTestData() throws Exception {
		isupplementRepository.deleteAll();
		csupplementRepository.deleteAll();
		pfsupplementRepository.deleteAll();
		pfRepository.deleteAll();
		itemRepository.deleteAll();
		batchSupplementFileRepository.deleteAll();
		batchFileRepository.deleteAll();
		batchRepository.deleteAll();
		
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
        
        Optional<AmpUser> users = ampUserRepository.findByUsername("dan");
        
        ValidationResponse response = manifestService.validate("Test Unit", "Test File", users.get(), content);
        
        Assert.assertFalse(response.hasErrors());
        
        boolean success = batchService.processBatch(response, "Test User");
        
        Assert.assertTrue(success);
        
	}
	
}


