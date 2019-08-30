package edu.indiana.dlib.amppd.service;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class WorkflowServiceTests {
		
	@Autowired
	private WorkflowService workflowService;   
		
	@Before
	public void setup() {
		// TODO We need to make sure there're some existing workflows in Galaxy for testing;
		// this can be done via factory to import workflow json files, or populate workflows with Galaxy bootstrap.
				
		// TODO alternatively we could use mock workflowsClient, in which case we won't require any existing data in Galaxy
 	}

}
