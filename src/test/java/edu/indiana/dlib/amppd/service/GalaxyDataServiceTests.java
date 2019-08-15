package edu.indiana.dlib.amppd.service;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.beans.GalaxyObject;
import com.github.jmchilton.blend4j.galaxy.beans.Library;

import edu.indiana.dlib.amppd.exception.GalaxyFileUploadException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GalaxyDataServiceTests {

	public static String UNIT_TEST_DIR = "ut";
	public static String UNIT_TEST_FILE = "foo";
	
	@Autowired
    private FileStorageService fileStorageService;

	@Autowired
	private GalaxyDataServiceImpl galaxyDataService;   

    @After
    public void cleanAll() {
    	// clean up unit test directory after unit tests done
        fileStorageService.delete(UNIT_TEST_DIR);
    }

    /**
     * Create a temporary file under amppd file system root for unit tests and return the absolute pathname.
     */
    private String createTmpFile() {
    	Path unitPath = fileStorageService.resolve(UNIT_TEST_DIR);
    	String pathname = UNIT_TEST_DIR + "/" + UNIT_TEST_FILE;
    	Path path = fileStorageService.resolve(pathname);
    	
    	try {
    		Files.createDirectories(unitPath);
    		Files.createFile(path);
    	}        
    	catch (FileAlreadyExistsException e) {
        	// if the file already exists do nothing
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Can't create temporary file for testing UploadFileToSharedLibrary.", e);
    	}
    	
		return path.toAbsolutePath().toString();
    }
    
    @Test
    public void shouldReturnSharedLibrary() {
    	Library lbirary = galaxyDataService.getLibrary(GalaxyDataServiceImpl.SHARED_LIBARY_NAME);
    	Assert.assertEquals(lbirary.getName(), GalaxyDataServiceImpl.SHARED_LIBARY_NAME);
    }

    @Test
    public void shouldReturnNullForNonExistingLibrary() {
    	Library lbirary = galaxyDataService.getLibrary("foo");
    	Assert.assertNull(lbirary);
    }

    @Test
    public void shouldUploadFileToSharedLibrary() {
    	String pathname = createTmpFile();
    	GalaxyObject dataset = galaxyDataService.uploadFileToGalaxy(pathname);
    	Assert.assertNotNull(dataset);
    	Assert.assertNotNull(dataset.getId());
    	Assert.assertNotNull(dataset.getUrl());
    }
    
    @Test(expected = GalaxyFileUploadException.class)
    public void shouldThrowExceptionUploadingToNonExistingLibrary() {
    	String pathname = createTmpFile();
    	galaxyDataService.uploadFileToGalaxy(pathname, "bar");
    }
    

    
}
