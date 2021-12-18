package edu.indiana.dlib.amppd.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.util.TestHelper;

//@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest
public class DropboxServiceTests {

    public static final String[] INVALID_CHARS = new String[] { "[", "]",   ",", ";", " ", ":", "/", "?",  "#", "=", "@", "!",  "$", "&", "'", "(", ")", "*","+"};

	@Autowired
	private DropboxService dropboxService;
	
	@Autowired
	private TestHelper testHelper;
	
	@Test
    public void shouldHandleInvalidCharacters() {
    	String path = "";
    	String originalPath = "TEST1._-";
    	String lastChar = " ";
    	for(String character : INVALID_CHARS) {
    		path = originalPath + character;
    		assertFalse(validPath(path));
        	String encodedValue = dropboxService.encodeUri(path);
        	System.out.println("Original Value: " + path + " Encoded Value: " + encodedValue);
    		assertTrue(validPath(encodedValue));
    		assertTrue(encodedValue.contains(originalPath));

    		path = character + originalPath + character;
    		encodedValue = dropboxService.encodeUri(path);
        	System.out.println("Original Value: " + path + " Encoded Value: " + encodedValue);
    		assertTrue(validPath(encodedValue)); 
    		assertTrue(encodedValue.contains(originalPath));   	
    		
    		path = character + originalPath + lastChar + originalPath + character;
    		encodedValue = dropboxService.encodeUri(path);
        	System.out.println("Original Value: " + path + " Encoded Value: " + encodedValue);
    		assertTrue(validPath(encodedValue));    
    		assertTrue(encodedValue.contains(originalPath));  
    		lastChar = character;
    	}
    	
    }
        
    private boolean validPath(String path) {
    	for(String character : INVALID_CHARS) {
    		if(path.contains(character)) return false;
    	}
    	return true;
    }

    @Ignore
    @Test
    public void shouldRenameSubdirForUnit() {
    	Collection collection = testHelper.ensureCollection("Test Unit", "Test Collection");
    	Unit unit = collection.getUnit();
    	Path pathOld = dropboxService.getSubDirPath(unit);
    	unit.setName("Test Unit Updated");
    	Path path = dropboxService.renameSubdir(unit);
    	Path pathCol = dropboxService.getSubDirPath(collection);
    	assertFalse(Files.exists(pathOld));    	
    	assertTrue(Files.exists(path));    	
    	assertTrue(Files.exists(pathCol));    	
    }
    
    @Test
    public void shouldDeleteSubdirForUnit() {
    	Collection collection = testHelper.ensureCollection("Test Unit", "Test Collection");    
    	Unit unit = collection.getUnit();
    	Path path = dropboxService.deleteSubdir(unit);
    	assertFalse(Files.exists(path));    	   	
    }
        
    @Test
    public void shouldCreateSubdirForCollection() {
    	Collection collection = new Collection();
    	Unit unit = testHelper.ensureUnit("Test Unit");
    	collection.setUnit(unit);
    	collection.setName("Test Collection");
    	Path path = dropboxService.createSubdir(collection);    	
    	assertTrue(Files.exists(path));    	
    }
    
    @Test
    public void shouldRenameSubdirForCollection() {
    	Collection collection = testHelper.ensureCollection("Test Unit", "Test Collection");
    	Path pathOld = dropboxService.getSubDirPath(collection);
    	collection.setName("Test Collection Updated");
    	Path path = dropboxService.renameSubdir(collection);
    	assertFalse(Files.exists(pathOld));    	
    	assertTrue(Files.exists(path));    	
    }
    
    @Test
    public void shouldDeleteSubdirForCollection() {
    	Collection collection = testHelper.ensureCollection("Test Unit", "Test Collection");    	
    	Path path = dropboxService.deleteSubdir(collection);
    	assertFalse(Files.exists(path));    	   	
    }
    
    
}
