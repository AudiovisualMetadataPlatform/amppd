package edu.indiana.dlib.amppd.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.service.impl.GalaxyApiServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GalaxyApiServiceTests {
	
	@Autowired
	private GalaxyPropertyConfig config;

	@Autowired
	private GalaxyApiServiceImpl galaxyApiService;   

//	private GalaxyApiService galaxyApiService = Mockito.spy(new GalaxyApiServiceImpl());   
	
//    @Before
//    public void setUpService() {
//    	galaxyApiService = Mock.spy(GalaxyApiServiceImpl.class);
//    }

//    @Before
//    public void initMocks() {
//        MockitoAnnotations.initMocks(this);
//    }
    
    // TODO add tests for getCurrentUser after its proper implementation and update following test 
    
    @Test
    public void shouldReturnInstanceForValidUser() {
//    	Mockito.when(galaxyApiService.getCurrentUser()).thenReturn(new GalaxyUser(config.getUsername(), config.getPassword()));
    	GalaxyInstance instance = galaxyApiService.getGalaxyInstance();
    	Assert.assertNotNull(instance);
    	Assert.assertEquals(instance.getGalaxyUrl(), config.getBaseUrl());
    }

//    @Test(expected = RuntimeException.class)
//    public void shouldThrowExceptionForInvalidUser() {
//    	Mockito.when(galaxyApiService.getCurrentUser()).thenReturn(new GalaxyUser("foo", "bar"));
//    	galaxyApiService.getInstance();
//    }

    
}
