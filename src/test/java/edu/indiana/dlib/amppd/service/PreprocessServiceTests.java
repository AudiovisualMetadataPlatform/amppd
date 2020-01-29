package edu.indiana.dlib.amppd.service;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.model.Asset;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PreprocessServiceTests {
	
	public static final String TEST_DIR_NAME = "test";
	
	@Autowired
    private PreprocessService preprocessService;

	@Autowired
	private TestHelper testHelper;   
	
    @Before
    public void cleanAll() {
    	// clean up primaryfiles in DB to avoid reusing the same one among different tests
    	testHelper.cleanupPrimaryfiles();
    }

	@Test
    public void shouldConvertFlacToWav() {
		Asset primaryfile = testHelper.ensurePrimaryfile(TestHelper.TEST_AUDIO, "flac");
		Assert.assertEquals("flac", FilenameUtils.getExtension(primaryfile.getPathname()).toLowerCase());

		// the media file should have been converted and the extension should be "wav"
		Asset updatedPrimaryfile = preprocessService.convertFlac(primaryfile);		
		Assert.assertEquals(primaryfile.getId(), updatedPrimaryfile.getId());
		Assert.assertEquals("wav", FilenameUtils.getExtension(updatedPrimaryfile.getPathname()).toLowerCase());
	}
	
	@Test
    public void shouldNotConvertNonFlacToWav() {
		Asset primaryfile = testHelper.ensurePrimaryfile(TestHelper.TEST_AUDIO, "mp3");
		String pathname = primaryfile.getPathname();
		
		// the media file should not have been converted and the pathname should be the same as before
		Asset updatedPrimaryfile = preprocessService.convertFlac(primaryfile);		
		Assert.assertEquals(primaryfile.getId(), updatedPrimaryfile.getId());
		Assert.assertEquals(pathname, updatedPrimaryfile.getPathname());
	}
	
	@Test
    public void shouldRetrievePrimaryfileMediaInfo() {
		Asset primaryfile = testHelper.ensurePrimaryfile(TestHelper.TEST_VIDEO, "mp4");

		// the media info should have been retrieved
		Asset updatedPrimaryfile = preprocessService.preprocess(primaryfile);
		Assert.assertEquals(primaryfile.getId(), updatedPrimaryfile.getId());
		String mediaInfo = updatedPrimaryfile.getMediaInfo();
		Assert.assertTrue(StringUtils.isNotEmpty(mediaInfo));
		Assert.assertTrue(mediaInfo.contains("container"));
		Assert.assertTrue(mediaInfo.contains("duration"));
		Assert.assertTrue(mediaInfo.contains("format"));
		Assert.assertTrue(mediaInfo.contains("mime_type"));
		Assert.assertTrue(mediaInfo.contains("size"));		
		Assert.assertTrue(mediaInfo.contains("streams"));		
		Assert.assertTrue(mediaInfo.contains("audio"));		
		Assert.assertTrue(mediaInfo.contains("video"));		
	}
	
}
