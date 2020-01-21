package edu.indiana.dlib.amppd.service;

import org.apache.commons.io.FilenameUtils;
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
	private TestHelper testHelper;   
	
    @After
//    public void cleanAll() {
//    	// clean up unit test directory after unit tests done
//        fileStorageService.delete(TEST_DIR_NAME);
//    }

	@Test
    public void shouldConver() {
		Primaryfile primaryfile = testHelper.ensurePrimaryfile(TestHelper.TEST_AUDIO, "flac");

		// the media file should have been converted and the extension should be "wav"
		Assert.assertTrue(preprocessService.convertFlac(primaryfile));		
		Assert.assertEquals("wav", FilenameUtils.getExtension(primaryfile.getPathname()).toLowerCase());
	}
	
	@Test
    public void shouldNotConverNonFlacToWav() {
		Primaryfile primaryfile = testHelper.ensurePrimaryfile(TestHelper.TEST_AUDIO, "mp3");
		String pathname = primaryfile.getPathname();
		
		// the media file should not have been converted and the pathname should be the same as before
		Assert.assertFalse(preprocessService.convertFlac(primaryfile));		
		Assert.assertEquals(pathname, primaryfile.getPathname());
	}
	

}
