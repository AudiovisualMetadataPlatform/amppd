package edu.indiana.dlib.amppd.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.model.Asset;
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
    public void shouldConvertFlacToWav() {
		Asset primaryfile = testHelper.ensurePrimaryfile(TestHelper.TEST_AUDIO, "flac");

		// the media file should have been converted and the extension should be "wav"
		Asset updatedPrimaryfile = preprocessService.convertFlac(primaryfile);		
		Assert.assertEquals(primaryfile.getId(), updatedPrimaryfile.getId());
		Assert.assertEquals("flac", FilenameUtils.getExtension(primaryfile.getPathname()).toLowerCase());
		Assert.assertEquals("wav", FilenameUtils.getExtension(updatedPrimaryfile.getPathname()).toLowerCase());
	}
	
	@Test
    public void shouldNotConvertNonFlacToWav() {
		Asset primaryfile = testHelper.ensurePrimaryfile(TestHelper.TEST_AUDIO, "mp3");
		
		// the media file should not have been converted and the pathname should be the same as before
		Asset updatedPrimaryfile = preprocessService.convertFlac(primaryfile);		
		Assert.assertEquals(primaryfile.getId(), updatedPrimaryfile.getId());
		Assert.assertEquals(primaryfile.getPathname(), updatedPrimaryfile.getPathname());
	}
	
	@Test
    public void shouldRetrievePrimaryfileMediaInfo() {
		Asset primaryfile = testHelper.ensurePrimaryfile(TestHelper.TEST_VIDEO, "mp4");

		// the media info should have been retrieved
		Asset updatedPrimaryfile = preprocessService.preprocess(primaryfile);
		Assert.assertEquals(primaryfile.getId(), updatedPrimaryfile.getId());
		Assert.assertTrue(StringUtils.isNotEmpty(updatedPrimaryfile.getMediainfo()));
	}
	
}
