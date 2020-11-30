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
import edu.indiana.dlib.amppd.model.CollectionSupplement;
import edu.indiana.dlib.amppd.model.Primaryfile;
import edu.indiana.dlib.amppd.model.Supplement.SupplementType;
import edu.indiana.dlib.amppd.model.WorkflowResult;
import edu.indiana.dlib.amppd.repository.PrimaryfileRepository;
import edu.indiana.dlib.amppd.service.impl.MediaServiceImpl;
import edu.indiana.dlib.amppd.util.TestHelper;
import edu.indiana.dlib.amppd.web.ItemSearchResponse;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MediaServiceTests {

	@Autowired
    private PrimaryfileRepository primaryfileRepository;
	
	@Autowired
	private MediaService mediaService;

	@Autowired
	private TestHelper testHelper;   

	private Primaryfile primaryfile, primaryfileS;
	private CollectionSupplement collectionSupplement;

	@Before
	public void setup() {
		// prepare the primaryfile with empty symlink for testing
		primaryfile = testHelper.ensureTestAudio();
		primaryfile.setSymlink(null);
		
		// prepare the collectionSupplement zip file for testing
		primaryfileS = testHelper.ensureTestVideo();
		collectionSupplement = testHelper.ensureTestCollectionSupplementZip(primaryfileS);
	}

	@After
	public void cleanup() {
		// remove all symlinks created
		mediaService.cleanup();
	}

	@Test
    public void shouldReturnCollectionSupplementPathname() {    	      
		String pathname = mediaService.getSupplementPathname(primaryfileS, TestHelper.TEST_SUPPLEMENT, SupplementType.COLLECTION);
		Assert.assertNotNull(pathname);
		Assert.assertTrue(pathname.endsWith(".zip"));
	}

	@Test
    public void shouldReturnNullForNonExistingCollectionSupplement() {    	      
		String pathname = mediaService.getSupplementPathname(primaryfileS, "foo", SupplementType.COLLECTION);
		Assert.assertNull(pathname);
	}
	
	@Test
    public void shouldReturnNullForNullPrimaryfileAssociatedSupplement() {    	      
		String pathname = mediaService.getSupplementPathname(null, TestHelper.TEST_SUPPLEMENT,  SupplementType.COLLECTION);
		Assert.assertNull(pathname);
	}
	
	@Test
    public void shouldReturnNullForNonPreparedItemSupplement() {    	      
		String pathname = mediaService.getSupplementPathname(primaryfileS, TestHelper.TEST_SUPPLEMENT,  SupplementType.ITEM);
		Assert.assertNull(pathname);
	}
	
	@Test
    public void shouldReturnPrimaryfileMediaUrl() {    	      
		String url = mediaService.getPrimaryfileMediaUrl(primaryfile);
		Assert.assertTrue(url.startsWith("http://"));
		Assert.assertTrue(url.contains("/primaryfiles/" + primaryfile.getId()));
		Assert.assertTrue(url.endsWith("/media"));
	}
    
	@Test
    public void shouldReturnPrimaryfileMediaInfoPath() {    	      
		String path = mediaService.getAssetMediaInfoPath(primaryfile);
		Assert.assertTrue(path.contains(primaryfile.getId().toString()));
		Assert.assertTrue(path.endsWith(".json"));
	}
	
	@Test
    public void shouldCreateNewPrimaryfileSymlink() {
		// initially symlink in primaryfile is not populated
		Assert.assertNull(primaryfile.getSymlink());
		
		mediaService.createSymlink(primaryfile);	
		
		// after creating, symlink in primaryfile should be populated
		Assert.assertNotNull(primaryfile.getSymlink());
		
		// and the symlink starts with primaryfile ID
		Assert.assertTrue(primaryfile.getSymlink().contains(primaryfile.getId().toString()));
		
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
    
	@Test
    public void shouldReturnWorkflowResultOutputUrl() {    	      
		String url = mediaService.getWorkflowResultOutputUrl(1L);
		Assert.assertTrue(url.startsWith("http://"));
		Assert.assertTrue(url.contains("/workflow-result/" + 1));
		Assert.assertTrue(url.endsWith("/output"));
	}
    	
	@Test
	public void shouldReturnTxtExtensionForTxtOutput() {
		WorkflowResult WorkflowResultResult = new WorkflowResult();
		WorkflowResultResult.setOutputType(MediaServiceImpl.TYPE_TXT.get(0));
		String extension = mediaService.getWorkflowResultOutputExtension(WorkflowResultResult);
		Assert.assertEquals(extension, MediaServiceImpl.FILE_EXT_TXT);
	}
	
	@Test
	public void shouldReturnJsonExtensionForJsonOutput() {
		WorkflowResult WorkflowResultResult = new WorkflowResult();
		WorkflowResultResult.setOutputType(MediaServiceImpl.TYPE_JSON.get(0));
		String extension = mediaService.getWorkflowResultOutputExtension(WorkflowResultResult);
		Assert.assertEquals(extension, MediaServiceImpl.FILE_EXT_JSON);
	}
	
	@Test
	public void shouldReturnAudioExtensionForAudioOutput() {
		WorkflowResult WorkflowResultResult = new WorkflowResult();
		WorkflowResultResult.setOutputType(MediaServiceImpl.TYPE_AUDIO.get(0));
		String extension = mediaService.getWorkflowResultOutputExtension(WorkflowResultResult);
		Assert.assertEquals(extension, MediaServiceImpl.FILE_EXT_AUDIO);
	}
	
	@Test
	public void shouldReturnStandardardExtensionAsIs() {
		WorkflowResult WorkflowResultResult = new WorkflowResult();
		WorkflowResultResult.setOutputType("png");
		String extension = mediaService.getWorkflowResultOutputExtension(WorkflowResultResult);
		Assert.assertEquals(extension, "png");
	}
	
	@Test
	public void shouldReturnVideoExtensionForVideoOutput() {
		WorkflowResult WorkflowResultResult = new WorkflowResult();
		WorkflowResultResult.setOutputType(MediaServiceImpl.TYPE_VIDEO.get(0));
		String extension = mediaService.getWorkflowResultOutputExtension(WorkflowResultResult);
		Assert.assertEquals(extension, MediaServiceImpl.FILE_EXT_VIDEO);
	}
	
	@Test
	public void shouldFindItemorFile() {
		//primaryfile = Fixture.from(Primaryfile.class).gimme("valid");
		System.out.println("------------primaryfile name is:"+primaryfile.getName());
		String keyword = new String(primaryfile.getName().substring(0,2));
		String mediaType = new String("audio");
		ItemSearchResponse response = mediaService.findItemOrFile(keyword, mediaType);
		System.out.println("-------------------success is:"+response.isSuccess());
		
		Assert.assertTrue(response.isSuccess());
	}

	
}
