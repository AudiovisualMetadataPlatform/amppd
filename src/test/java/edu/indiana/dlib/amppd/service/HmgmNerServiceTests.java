package edu.indiana.dlib.amppd.service;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
public class HmgmNerServiceTests {

	@Autowired
    private HmgmNerService hmgmNerService;
	
	File inputFile;
	File tmpFile;
	File completeFile;
	String testJson = "{key: value}";
    
	@Before
	public void setup() throws Exception {
		String fileName = "hmgm_ner.json";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		inputFile = new File(classLoader.getResource(fileName).getFile());
	    Assert.assertTrue(inputFile.exists());

	    completeFile = new File(inputFile.getAbsoluteFile() + ".complete");
	    tmpFile = new File(inputFile.getAbsoluteFile() + ".tmp");
	}
	
	@After
	public void cleanup() throws IOException {
		if(completeFile.exists()) completeFile.delete();
		if(tmpFile.exists()) tmpFile.delete();
	    Assert.assertFalse(completeFile.exists());
	    Assert.assertFalse(tmpFile.exists());
	}

	@Test
	public void shouldGetOriginalNer() throws Exception {	 
		// no tmp file at this point
	    Assert.assertFalse(tmpFile.exists());

	    String content = hmgmNerService.getNer(inputFile.getAbsolutePath());	   
	    
	    // original file is used
	    Assert.assertNotNull(content);
	    Assert.assertTrue(content.contains("http://iiif.io/api/presentation/3/context.json"));
	    // TODO add a string comparison with the original content
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


