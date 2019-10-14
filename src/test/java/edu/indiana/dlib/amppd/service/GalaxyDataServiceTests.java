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
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.Library;

import edu.indiana.dlib.amppd.exception.GalaxyFileUploadException;
import edu.indiana.dlib.amppd.service.impl.GalaxyDataServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GalaxyDataServiceTests {

	public static final String TEST_DIRECTORY_NAME = "test";
	public static final String TEST_FILE_NAME = "test.txt";
	public static final String TEST_LIBRARY_NAME = "ammpd-test";
	
	@Autowired
    private FileStorageService fileStorageService;

	@Autowired
	private GalaxyDataService galaxyDataService;   
	
	private String testFile;

	@Before
	public void setup() {
		createTestFile();
		createTestLibrary();
	}
	
    @After
    public void cleanup() {
    	// clean up unit test directory after unit tests done
        fileStorageService.delete(TEST_DIRECTORY_NAME);
        
        // TODO delete test library and all its contents
    }

    @Test
    public void shouldReturnSharedLibrary() {
    	Assert.assertEquals(galaxyDataService.getSharedLibrary().getName(), GalaxyDataServiceImpl.SHARED_LIBARY_NAME);
    }

    @Test
    public void shouldReturnSharedHistory() {
    	Assert.assertEquals(galaxyDataService.getSharedHistory().getName(), GalaxyDataServiceImpl.SHARED_HISTORY_NAME);
    }

    @Test
    public void shouldReturnTestLibrary() {
    	Library lbirary = galaxyDataService.getLibrary(TEST_LIBRARY_NAME);
    	Assert.assertEquals(lbirary.getName(), TEST_LIBRARY_NAME);
    }

    @Test
    public void shouldReturnNullForNonExistingLibrary() {
    	Library lbirary = galaxyDataService.getLibrary("bar");
    	Assert.assertNull(lbirary);
    }

    @Test
    public void shouldReturnNullForNonExistingHistory() {
    	History history = galaxyDataService.getHistory("bar");
    	Assert.assertNull(history);
    }

    @Test
    public void shouldUploadFileToExistingLibrary() {
    	GalaxyObject dataset = galaxyDataService.uploadFileToGalaxy(testFile, TEST_LIBRARY_NAME);
    	Assert.assertNotNull(dataset);
    	Assert.assertNotNull(dataset.getId());
    	Assert.assertNotNull(dataset.getUrl());
    }
    
    @Test(expected = GalaxyFileUploadException.class)
    public void shouldThrowExceptionUploadingToNonExistingLibrary() {
    	galaxyDataService.uploadFileToGalaxy(testFile, "bar");
    }
    
    /**
     * Create a temporary empty file under amppd file system root for unit tests and return the absolute pathname.
     */
    private void createTestFile() {
    	Path unitPath = fileStorageService.resolve(TEST_DIRECTORY_NAME);
    	String pathname = TEST_DIRECTORY_NAME + "/" + TEST_FILE_NAME;
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
    private Library createTestLibrary() {
		Library library = galaxyDataService.getLibrary(TEST_LIBRARY_NAME);
		if (library != null) {
			return library;
		}

		library = new Library(TEST_LIBRARY_NAME);
		library.setDescription("AMPPD Test Library");
		try {
			return galaxyDataService.getLibrariesClient().createLibrary(library);
		}
		catch (Exception e) {
			String msg = "Cannot create test Galaxy data library for GalaxyDataServiceTests.";
			throw new RuntimeException(msg, e);
		}		
    }
        
}
