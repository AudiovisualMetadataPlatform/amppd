package edu.indiana.dlib.amppd.service;



import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import edu.indiana.dlib.amppd.config.AmppdPropertyConfig;
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
import edu.indiana.dlib.amppd.repository.UnitRepository;
import edu.indiana.dlib.amppd.web.BatchValidationResponse;

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
	@Autowired
	private AmppdPropertyConfig propertyConfig;
	@Autowired
    private AmpUserService ampUserService;

	
	private String ampUsername = "ampTestUser";
	
	@Before
	public void createTestData() throws Exception {
        // Cleanup drop box root for testing
        deleteDirectory(new File(propertyConfig.getFileStorageRoot()), false);
        
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
		
		String collectionName = "Music Library";
		String unitName = "Test Unit";
		
		// Make sure a test unit and collection are created
		mockMvc.perform(post("/units").content(
				"{\"name\": \"" + unitName + "\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andExpect(
								header().string("Location", containsString("units/")));

		mockMvc.perform(post("/collections").content(
				"{ \"name\": \"" + collectionName + "\", \"description\":\"For test\"}")).andExpect(
						status().isCreated()).andReturn();
		
		
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		 
		
		// If we don't have a test user, create one
	 	Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
	 	if(users==null || users.isEmpty()) {
	 		AmpUser user = new AmpUser();
		 	user.setUsername(ampUsername);
		 	user.setPassword("testAmpPassword");
		 	user.setEmail("ampTestUser@iu.edu");
		 	ampUserService.registerAmpUser(user);
	 	}
	 	
		// Copy zip file and extract the files
        File srcFile = new File(classLoader.getResource("testfiles.zip").getFile());	
        
        Assert.assertTrue(srcFile.exists());
        
        // Create necessary directories

        Files.createDirectories(Paths.get(propertyConfig.getDropboxRoot()));
        Files.createDirectories(Paths.get(propertyConfig.getDropboxRoot(), unitName));
        Files.createDirectories(Paths.get(propertyConfig.getDropboxRoot(), unitName, collectionName));
        
        Path destPath = Paths.get(propertyConfig.getDropboxRoot(), unitName, collectionName, "test_files.zip");	

		Files.copy(srcFile.toPath(), destPath);
		
        unzip(destPath.toString(), Paths.get(propertyConfig.getDropboxRoot(), unitName, collectionName).toString());
        
        Files.delete(destPath);
        
	}
	boolean deleteDirectory(File directoryToBeDeleted, boolean deleteOriginal) {
	    File[] allContents = directoryToBeDeleted.listFiles();
	    if (allContents != null) {
	        for (File file : allContents) {
	            deleteDirectory(file, true);
	        }
	    }
	    if(deleteOriginal) {
		    if(!new File(propertyConfig.getDropboxRoot()).equals(directoryToBeDeleted)) {
		    	directoryToBeDeleted.delete();
		    }
	    }
	    return true;
	}
	
	@After
	public void cleanup() throws IOException {
		FileUtils.cleanDirectory(new File(Paths.get(propertyConfig.getDropboxRoot(), "Test Unit", "Music Library").toString())); 
	}
	
	// TODO remove ignore once we have ffmpeg and MediaProbe installed on Bamboo
	@Ignore
	@Test
	public void shouldBeValid() throws Exception {
		String fileName = "batch_manifest_for_testing.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        
        System.out.println(content);
        
        Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
        
        BatchValidationResponse response = manifestService.validate("Test Unit", "Test File", users.get(), content);

        if(response.getErrors()!=null) {
            for(String s : response.getErrors()) {
                System.out.println("Should be valid fails validation: " + s);
            }
        }
        
        Assert.assertFalse(response.hasErrors());
        
        boolean success = batchService.processBatch(response, ampUsername);
        
        
        Assert.assertTrue(success);
        
	}
	
	@Test
	public void shouldBeValidManifest() throws Exception {
		String fileName = "batch_manifest_for_testing.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        
        System.out.println(content);
        
        Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
        
        BatchValidationResponse response = manifestService.validate("Test Unit", "Test File", users.get(), content);
        
        Assert.assertFalse(response.hasErrors());
	}
	
	/*
	 * Invalid unit name.  Should fail validation
	 */
	@Test
	public void shouldBeInvalidUnitName() throws Exception {
		String fileName = "batch_manifest_for_testing.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        
        System.out.println(content);
                
        Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
        
        BatchValidationResponse response = manifestService.validate("Invalid Unit", "Test File", users.get(), content);
        
        Assert.assertTrue(response.hasErrors());
	}
	
	/*
	 * File name doesn't exist.  Should fail validation
	 */
	@Test
	public void shouldBeInvalidFile() throws Exception {
		String fileName = "batch_manifest_blank.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
        
        System.out.println(content);
                
        Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
        
        BatchValidationResponse response = manifestService.validate("Test Unit", "Test File", users.get(), content);
        
        Assert.assertTrue(response.hasErrors());
	}

	/*
	 * Invalid primary file.  Should fail validation
	 */
	@Test
	public void shouldBeInvalidPrimaryFile() throws Exception {
		String fileName = "batch_manifest_invalid_primary_file.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
                        
        Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
        
        BatchValidationResponse response = manifestService.validate("Test Unit", "Test File", users.get(), content);
        
        Assert.assertTrue(response.hasErrors());
	}
	
	/*
	 * Duplicate primary file names.  Should fail validation
	 */
	@Test
	public void shouldBeInvalidPrimaryFileDuplicate() throws Exception {
		String fileName = "batch_manifest_duplicate_primary.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));
                        
        Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
        
        BatchValidationResponse response = manifestService.validate("Test Unit", "Test File", users.get(), content);
        
        Assert.assertTrue(response.hasErrors());
	}
	
	/*
	 * Duplicate supplement file names.  Should fail validation
	 */
	@Test
	public void shouldBeInvalidSupplementFileDuplicate() throws Exception {
		String fileName = "batch_manifest_duplicate_supplement.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));

        Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
        
        BatchValidationResponse response = manifestService.validate("Test Unit", "Test File", users.get(), content);
        
        Assert.assertTrue(response.hasErrors());
	}
	
	/*
	 * Invalid primary file with supplement file type = primary
	 */
	@Test
	public void shouldBeInvalidPrimaryFileWithPrimarySupplement() throws Exception {
		String fileName = "batch_manifest_invalid_primary_file_2.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));

        Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
        
        BatchValidationResponse response = manifestService.validate("Test Unit", "Test File", users.get(), content);
        
        Assert.assertTrue(response.hasErrors());
	}
	
	/*
	 * Invalid supplement file
	 */
	@Test
	public void shouldBeInvalidSuppFile() throws Exception {
		String fileName = "batch_manifest_invalid_supp_file.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
 
        File file = new File(classLoader.getResource(fileName).getFile());
        String content = new String(Files.readAllBytes(file.toPath()));

        Optional<AmpUser> users = ampUserRepository.findByUsername(ampUsername);
        
        BatchValidationResponse response = manifestService.validate("Test Unit", "Test File", users.get(), content);
        
        Assert.assertTrue(response.hasErrors());
	}
	
	/*
	 * Helper method to unzip zip file to destination directory
	 */
	private void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
	/*
	 * Extracts individual file from zip
	 */
	private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
	
}


