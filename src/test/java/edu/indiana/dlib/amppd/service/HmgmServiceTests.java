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

import edu.indiana.dlib.amppd.web.SaveTranscriptRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HmgmServiceTests {

	@Autowired
    private HmgmService hmgmService;

	
	File testFile;
	File tempFile;
	File completeFile;
	String testJson="{}";
    
	@Before
	public void createTestData() throws Exception {
		String fileName = "batch_manifest_for_testing.csv";
		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
		testFile = new File(classLoader.getResource(fileName).getFile());
	    Assert.assertTrue(testFile.exists());

	    completeFile = new File(testFile.getAbsoluteFile() + ".complete");

	    tempFile = new File(testFile.getAbsoluteFile() + ".tmp");
	}
	
	@After
	public void cleanup() throws IOException {
		if(completeFile.exists()) completeFile.delete();
		if(tempFile.exists()) tempFile.delete();
	}

	@Test
	public void shouldGetFile() throws Exception {	    
	    
	    TranscriptEditorResponse response = hmgmService.getTranscript(testFile.getAbsolutePath(), false);
	    
	    Assert.assertTrue(response.isSuccess());
	    Assert.assertFalse(response.isComplete());
	    Assert.assertTrue(response.getContent().length()>0);
	}
	
	@Test
	public void shouldBeComplete() throws Exception {	    
	    
	    TranscriptEditorRequest request = new TranscriptEditorRequest();
	    request.setFilePath(testFile.getAbsolutePath());
	    
	    boolean success = hmgmService.completeTranscript(request);
	    
	    Assert.assertTrue(success);
	    Assert.assertTrue(completeFile.exists());

	    TranscriptEditorResponse response = hmgmService.getTranscript(testFile.getAbsolutePath(), false);
	    
	    Assert.assertTrue(response.isComplete());
	}

	@Test
	public void shouldCreateTemporaryFile() throws Exception {
	    
	    SaveTranscriptRequest request = new SaveTranscriptRequest();
	    request.setFilePath(testFile.getAbsolutePath() + ".tmp");
	    request.setJson(testJson);
	    
	    boolean success = hmgmService.saveTranscript(request);
	    
	    Assert.assertTrue(success);
	    Assert.assertTrue(tempFile.exists());
	    
	    
	    String text = new String(Files.readAllBytes(tempFile.toPath()), "UTF8");
	    
	    Assert.assertEquals(text, testJson);		        
	}
	

	@Test
	public void shouldUseTemporaryFile() throws Exception {
	    
	    Assert.assertTrue(testFile.exists());
	    
	    SaveTranscriptRequest request = new SaveTranscriptRequest();
	    request.setFilePath(testFile.getAbsolutePath() + ".tmp");
	    request.setJson(testJson);
	    
	    boolean success = hmgmService.saveTranscript(request);
	    
	    Assert.assertTrue(success);
	    Assert.assertTrue(tempFile.exists());
	    
	    TranscriptEditorRequest completeRequest = new TranscriptEditorRequest();
	    completeRequest.setFilePath(testFile.getAbsolutePath());
	    
	    boolean completeSuccess = hmgmService.completeTranscript(completeRequest);
	    
	    Assert.assertTrue(completeSuccess);

	    
	    // Verify the temp file was used
	    String text = new String(Files.readAllBytes(completeFile.toPath()), "UTF8");
	    
	    Assert.assertEquals(text, testJson);
		        
	}
	
}


