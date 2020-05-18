package edu.indiana.dlib.amppd.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DropboxServiceTests {

	@Autowired
	private DropboxService dropboxService;
	
    @Test
    public void TestInvalidCharacters() {
    	String path = "";
    	String originalPath = "TEST1._-";
    	String lastChar = " ";
    	for(String character : invalidChars) {
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
    
    String[] invalidChars = new String[] { "[", "]",   ",", ";", " ", ":", "/", "?",  "#", "=", "@", "!",  "$", "&", "'", "(", ")", "*","+"};
    
    private boolean validPath(String path) {
    	for(String character : invalidChars) {
    		if(path.contains(character)) return false;
    	}
    	return true;
    }


}
