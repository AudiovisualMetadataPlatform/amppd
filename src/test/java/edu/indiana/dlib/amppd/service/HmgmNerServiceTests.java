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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.web.NerEditorRequest;
import edu.indiana.dlib.amppd.web.NerEditorResponse;
import edu.indiana.dlib.amppd.web.SaveNerRequest;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HmgmNerServiceTests {

	@Autowired
    private HmgmNerService hmgmNerService;
	
	File testFile;
	File tmpFile;
	File completeFile;
	String testJson="{key: value}";
    
	@Before
	public void setup() throws Exception {
		String fileName = "ner.json";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		testFile = new File(classLoader.getResource(fileName).getFile());
	    Assert.assertTrue(testFile.exists());

	    completeFile = new File(testFile.getAbsoluteFile() + ".complete");
	    tmpFile = new File(testFile.getAbsoluteFile() + ".tmp");
	}
	
	@After
	public void cleanup() throws IOException {
		if(completeFile.exists()) completeFile.delete();
		if(tmpFile.exists()) tmpFile.delete();
	}

	@Test
	public void shouldGetOriginalNer() throws Exception {	 
		// no tmp file at this point
	    Assert.assertFalse(tmpFile.exists());

	    String content = hmgmNerService.getNer(testFile.getAbsolutePath());	   
	    
	    // original file is used
	    Assert.assertNotNull(content);
	    // TODO add a string comparison with the original content
	}
	
	@Test
	public void shouldGetTmpNer() throws Exception {	
		// save once to ensure tmp file exists
	    boolean success = hmgmNerService.saveNer(testFile.getAbsolutePath(), testJson);
	    Assert.assertTrue(success);
	    Assert.assertTrue(tmpFile.exists());
	    
	    String content = hmgmNerService.getNer(testFile.getAbsolutePath());
	    
	    // the tmp file content is returned
	    Assert.assertEquals(content, testJson);		        
	}
		
	@Test
	public void shouldSaveNer() throws Exception {
		// before save, no tmp file
	    Assert.assertFalse(tmpFile.exists());
	    
	    boolean success = hmgmNerService.saveNer(testFile.getAbsolutePath(), testJson);
	    
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
	    
	    boolean success = hmgmNerService.completeNer(testFile.getAbsolutePath());	
	    
	    // after complete, complete file exists
	    Assert.assertTrue(success);
	    Assert.assertTrue(completeFile.exists());
	}

}


