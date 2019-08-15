package edu.indiana.dlib.amppd.service;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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

	public static final String TEST_DIR_NAME = "test";
	public static final String TEST_FILE_NAME = "test.txt";
	public static final String TEST_LIB_NAME = "ammpd-test";
	
	@Autowired
    private FileStorageService fileStorageService;

	@Autowired
	private GalaxyDataServiceImpl galaxyDataService;   
	
	private String testFile;
	private Library testLibrary;	

	@Before
	public void setup() {
		createTestFile();
		createTestLibrary();
	}
	
    @After
    public void cleanup() {
    	// clean up unit test directory after unit tests done
        fileStorageService.delete(TEST_DIR_NAME);
        
        // TODO delete test library and all its contents
    }

    @Test
    public void shouldReturnSharedLibrary() {
    	Assert.assertEquals(galaxyDataService.getSharedLibrary().getName(), GalaxyDataServiceImpl.SHARED_LIBARY_NAME);
    }

    @Test
    public void shouldReturnTestLibrary() {
    	Library lbirary = galaxyDataService.getLibrary(TEST_LIB_NAME);
    	Assert.assertEquals(lbirary.getName(), TEST_LIB_NAME);
    }

    @Test
    public void shouldReturnNullForNonExistingLibrary() {
    	Library lbirary = galaxyDataService.getLibrary("bar");
    	Assert.assertNull(lbirary);
    }

    @Test
    public void shouldUploadFileToExistingLibrary() {
    	GalaxyObject dataset = galaxyDataService.uploadFileToGalaxy(testFile, TEST_LIB_NAME);
    	Assert.assertNotNull(dataset);
    	Assert.assertNotNull(dataset.getId());
    	Assert.assertNotNull(dataset.getUrl());
    }
    
    @Test(expected = GalaxyFileUploadException.class)
    public void shouldThrowExceptionUploadingToNonExistingLibrary() {
    	galaxyDataService.uploadFileToGalaxy(testFile, "bar");
    }
    
    /**
     * Create a temporary file under amppd file system root for unit tests and return the absolute pathname.
     */
    private void createTestFile() {
    	Path unitPath = fileStorageService.resolve(TEST_DIR_NAME);
    	String pathname = TEST_DIR_NAME + "/" + TEST_FILE_NAME;
    	Path path = fileStorageService.resolve(pathname);
    	
    	try {
    		Files.createDirectories(unitPath);
    		Files.createFile(path);
    	}        
    	catch (FileAlreadyExistsException e) {
        	// if the file already exists do nothing
    	}
    	catch (Exception e) {
    		throw new RuntimeException("Can't create test file for GalaxyDataServiceTests.", e);
    	}
    	
    	testFile = path.toAbsolutePath().toString();
    }
    
    /**
     * Create a temporary Galaxy data library for testing.
     */
    private void createTestLibrary() {
		Library library = galaxyDataService.getLibrary(TEST_LIB_NAME);
		if (library != null) {
			testLibrary = library;
			return;
		}

		library = new Library(TEST_LIB_NAME);
		library.setDescription("AMPPD Test Library");
		try {
			testLibrary = galaxyDataService.getLibraryClient().createLibrary(library);
		}
		catch (Exception e) {
			String msg = "Cannot create test Galaxy data library for GalaxyDataServiceTests.";
			throw new RuntimeException(msg, e);
		}		
    }
        
}
