package edu.indiana.dlib.amppd.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.beans.Library;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GalaxyDataServiceTests {

	@Autowired
	private GalaxyDataServiceImpl galaxyDataService;   

    @Test
    public void shouldReturnSharedLibrary() {
    	Library lbirary = galaxyDataService.getLibrary(GalaxyDataServiceImpl.SHARED_LIBARY_NAME);
    	Assert.assertEquals(lbirary.getName(), GalaxyDataServiceImpl.SHARED_LIBARY_NAME);
    }

    @Test
    public void shouldReturnNullOnNonExistingLibrary() {
    	Library lbirary = galaxyDataService.getLibrary("foo");
    	Assert.assertNull(lbirary);
    }

    @Test
    public void shouldUploadFileToSharedLibrary() {
    	
    }
    
    
    
}
