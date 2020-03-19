package edu.indiana.dlib.amppd.service;

import java.nio.file.Files;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.exception.StorageException;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.util.TestHelper;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MediaServiceTests {

	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
	private MediaService mediaService;

	@Autowired
	private TestHelper testHelper;   

	private Primaryfile primaryfile;

	@Before
	public void setup() {
		// prepare the primaryfile with empty symlink for testing
		primaryfile = testHelper.ensureTestAudio();
		primaryfile.setSymlink(null);
	}

	@After
	public void cleanup() {
		// remove all symlinks created
		mediaService.cleanAll();
	}

	@Test
    public void shouldCreateNewPrimaryfileSymlink() {
		// initially symlink in primaryfile is not populated
		Assert.assertNull(primaryfile.getSymlink());
		
		String symlink = mediaService.createSymlink(primaryfile);	
		
		// after creating, symlink in primaryfile should be populated
		Assert.assertNotNull(primaryfile.getSymlink());
		
		// and the symlink starts with primaryfile ID
		Assert.assertTrue(primaryfile.getSymlink().startsWith(primaryfile.getId().toString()));
		
		// and the symlink file should exist
		Assert.assertTrue(Files.exists(mediaService.resolve(primaryfile.getSymlink())));
	}
    
	@Test
    public void shouldReuseExistingPrimaryfileSymlink() {
		String symlink1 = mediaService.createSymlink(primaryfile);	
		String symlink2 = mediaService.createSymlink(primaryfile);	
		
		// the two symlinks should equal since no new symlink is created after initial creation
		Assert.assertEquals(symlink1,  symlink2);
	}
    
	@Test
    public void shouldReturnPrimaryfileSymlinkUrl() {
		String url = mediaService.getPrimaryfileSymlinkUrl(primaryfile.getId());	
		Primaryfile pf = primaryfileRepository.findById(primaryfile.getId()).orElseThrow(() -> new StorageException("Primaryfile <" + primaryfile.getId() + "> does not exist!"));  
		Assert.assertTrue(url.startsWith("http://"));
		Assert.assertTrue(url.contains("/symlink/"));
		Assert.assertTrue(url.endsWith(pf.getSymlink()));
	}
    

}
