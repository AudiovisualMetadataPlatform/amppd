package edu.indiana.dlib.amppd.service;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileSystemUtils;


@RunWith(SpringRunner.class)
@SpringBootTest
public class HmgmNerServiceTests {

	public static final String TEST_DIR = "/tmp/test/";
	public static final String TEST_FILE = "hmgm_ner.json";

	@Autowired
    private HmgmNerService hmgmNerService;
	
	private File inputFile;
	private File tmpFile;
	private File completeFile;
	private String testJson = "{\"key\":\"value\"}";

	/**
	 * Create test directory and test files.
	 * @throws Exception
	 */
	@Before
	public void setup() throws Exception {
		// use a temporary test directory to keep all test files instead of using the original data files in project target directory
		// to avoid side effect on project files (ex, the original NER input file will be moved to the complete file)
		Files.createDirectories(Paths.get(TEST_DIR));
		
		// copy original data file into the test directory
		Path source = Paths.get(ClassLoader.getSystemClassLoader().getResource(TEST_FILE).toURI());
		Path target = Paths.get(TEST_DIR, TEST_FILE);
		if (!Files.exists(target) ) {
			Files.copy(source, target);
		}
		
		inputFile = new File(target.toString());
	    completeFile = new File(inputFile.getAbsolutePath() + ".complete");
	    tmpFile = new File(inputFile.getAbsoluteFile() + ".tmp");
	}
	
	/**
	 * Clean up test directory.
	 * @throws IOException
	 */
	@After
	public void cleanup() throws IOException {
		FileSystemUtils.deleteRecursively(Paths.get(TEST_DIR));
	}

	@Test
	public void shouldGetOriginalNer() throws Exception {	 
		// no tmp file at this point
	    Assert.assertFalse(tmpFile.exists());

	    String content = hmgmNerService.getNer(inputFile.getAbsolutePath());	   
	    
	    // original file is used
	    Assert.assertNotNull(content);
	    Assert.assertTrue(content.contains("iiif.io"));
	}
	
	@Test
	public void shouldGetTmpNer() throws Exception {	
		// save once to ensure tmp file exists
	    boolean success = hmgmNerService.saveNer(inputFile.getAbsolutePath(), testJson);
	    Assert.assertTrue(success);
	    Assert.assertTrue(tmpFile.exists());
	    
	    String content = hmgmNerService.getNer(inputFile.getAbsolutePath());
	    
	    // the tmp file content is returned
	    Assert.assertEquals(content, testJson);		        
	}
		
	@Test
	public void shouldSaveNer() throws Exception {
		// before save, no tmp file
	    Assert.assertFalse(tmpFile.exists());
	    
	    boolean success = hmgmNerService.saveNer(inputFile.getAbsolutePath(), testJson);
	    
	    // after save, tmp file exists
	    Assert.assertTrue(success);
	    Assert.assertTrue(tmpFile.exists());
	    
	    // the tmp file content is correct
	    String text = new String(Files.readAllBytes(tmpFile.toPath()), "UTF8");	    
	    Assert.assertEquals(text, testJson);		        
	}
	
	@Test
	public void shouldComplete() throws Exception {	   
		// before complete, no complete file
	    Assert.assertFalse(completeFile.exists());
	    
		// save once to ensure tmp file exists
	    boolean successSave = hmgmNerService.saveNer(inputFile.getAbsolutePath(), testJson);
	    Assert.assertTrue(successSave);
	    Assert.assertTrue(tmpFile.exists());

	    boolean successComplete = hmgmNerService.completeNer(inputFile.getAbsolutePath());	
	    
	    // after complete, complete file exists, and tmp file doesn't exist
	    Assert.assertTrue(successComplete);
	    Assert.assertTrue(completeFile.exists());
	    Assert.assertFalse(tmpFile.exists());
	}

}


