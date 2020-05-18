package edu.indiana.dlib.amppd.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import edu.indiana.dlib.amppd.model.Collection;
import edu.indiana.dlib.amppd.model.Unit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DropboxServiceTests {

    public static final String[] INVALID_CHARS = new String[] { "[", "]",   ",", ";", " ", ":", "/", "?",  "#", "=", "@", "!",  "$", "&", "'", "(", ")", "*","+"};

	@Autowired
	private DropboxService dropboxService;
	
    @Test
    public void shouldHangleInvalidCharacters() {
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

    public void shouldCreateSubdirForCollection() {
    	Unit unit = new Unit();
    	unit.setId(1l);
    	unit.setName("Test Unit");
    	
    	Collection collection = new Collection();
    	collection.setId(2l);
    	collection.setName("Test Collection");
    	collection.setUnit(unit);
    	
    	Path path = dropboxService.createCollectionSubdir(collection);
    	assertTrue(Files.exists(path));    	
    }
    
    
}
