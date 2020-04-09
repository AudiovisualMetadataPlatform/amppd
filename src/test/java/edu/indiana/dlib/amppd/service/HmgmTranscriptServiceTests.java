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

import edu.indiana.dlib.amppd.web.SaveTranscriptRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorRequest;
import edu.indiana.dlib.amppd.web.TranscriptEditorResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
public class HmgmTranscriptServiceTests {

	public static final String TEST_DIR = "/tmp/test/";
	public static final String TEST_FILE = "hmgm_ner.json";

	@Autowired
    private HmgmTranscriptService hmgmTranscriptService;

	
	File testFile;
	File tempFile;
	File completeFile;
	String testJson="{}";
    
	@Before
	public void createTestData() throws Exception {
//		String fileName = "hmgm_transcribe.json";
//		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
//		testFile = new File(classLoader.getResource(fileName).getFile());
//	    Assert.assertTrue(testFile.exists());

		// use a temporary test directory to keep all test files instead of using the original data files in project target directory
		// to avoid side effect on project files (ex, the original NER input file will be moved to the complete file)
		Files.createDirectories(Paths.get(TEST_DIR));
		
		// copy original data file into the test directory
		Path source = Paths.get(ClassLoader.getSystemClassLoader().getResource(TEST_FILE).toURI());
		Path target = Paths.get(TEST_DIR, TEST_FILE);
		if (!Files.exists(target) ) {
			Files.copy(source, target);
		}
		
		testFile = new File(target.toString());	    
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
	    
	    TranscriptEditorResponse response = hmgmTranscriptService.getTranscript(testFile.getAbsolutePath(), false);
	    
	    Assert.assertTrue(response.isSuccess());
	    Assert.assertFalse(response.isComplete());
	    Assert.assertTrue(response.getContent().length()>0);
	}
	
	@Test
	public void shouldBeComplete() throws Exception {	    
	    
	    TranscriptEditorRequest request = new TranscriptEditorRequest();
	    request.setFilePath(testFile.getAbsolutePath());
	    
	    boolean success = hmgmTranscriptService.completeTranscript(request);
	    
	    Assert.assertTrue(success);
	    Assert.assertTrue(completeFile.exists());

	    TranscriptEditorResponse response = hmgmTranscriptService.getTranscript(testFile.getAbsolutePath(), false);
	    
	    Assert.assertTrue(response.isComplete());
	}

	@Test
	public void shouldCreateTemporaryFile() throws Exception {
	    
	    SaveTranscriptRequest request = new SaveTranscriptRequest();
	    request.setFilePath(testFile.getAbsolutePath() + ".tmp");
	    request.setJson(testJson);
	    
	    boolean success = hmgmTranscriptService.saveTranscript(request);
	    
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
	    
	    boolean success = hmgmTranscriptService.saveTranscript(request);
	    
	    Assert.assertTrue(success);
	    Assert.assertTrue(tempFile.exists());
	    
	    TranscriptEditorRequest completeRequest = new TranscriptEditorRequest();
	    completeRequest.setFilePath(tempFile.getAbsolutePath());
	    
	    boolean completeSuccess = hmgmTranscriptService.completeTranscript(completeRequest);
	    
	    Assert.assertTrue(completeSuccess);

	    
	    // Verify the temp file was used
	    String text = new String(Files.readAllBytes(completeFile.toPath()), "UTF8");
	    
	    Assert.assertEquals(text, testJson);
		        
	}
	
}


