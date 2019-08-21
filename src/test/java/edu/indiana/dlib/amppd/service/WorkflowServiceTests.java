package edu.indiana.dlib.amppd.service;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.github.jmchilton.blend4j.galaxy.beans.Library;

import edu.indiana.dlib.amppd.service.impl.GalaxyDataServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowServiceTests {
	public static final String TEST_DIR_NAME = "test";
	public static final String TEST_FILE_NAME = "test.txt";
	public static final String TEST_LIB_NAME = "ammpd-test";
	
	@Autowired
	private WorkflowService workflowService;   
		
	private String testFile;
	private Library testLibrary;	

    @Test
    public void shouldReturnSharedHistory() {
    	Assert.assertNotNull(workflowService.getSharedHistory());
    }


}
