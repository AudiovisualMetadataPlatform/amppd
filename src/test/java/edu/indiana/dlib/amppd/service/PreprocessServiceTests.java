package edu.indiana.dlib.amppd.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PreprocessServiceTests {
	
	public static final String TEST_DIR_NAME = "test";
	
	@Autowired
    private PreprocessService preprocessService;

	@Autowired
    private FileStorageService fileStorageService;

	@Autowired
	private TestHelper testHelper;   
	
    @After
    public void cleanAll() {
    	// clean up unit test directory after unit tests done
        fileStorageService.delete(TEST_DIR_NAME);
    }

	@Test
    public void shouldConverFlacToWav() {
		Primaryfile primaryfile = testHelper.ensurePrimaryfile(testHelper.TEST_VIDEO, "flac");
		boolean converted = preprocessService.convertFlac(primaryfile);
		Assert.assertTrue(converted);
	}
	
}
