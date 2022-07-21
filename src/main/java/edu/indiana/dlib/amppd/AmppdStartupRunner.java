package edu.indiana.dlib.amppd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.service.MgmRefreshService;

@Component
public class AmppdStartupRunner  implements CommandLineRunner {

	@Autowired
	private MgmRefreshService mgmRefreshService;

    @Override
    public void run(String...args) throws Exception {		
		// initialize/refresh MGM tables
		mgmRefreshService.refreshMgmTables();
    }
    
}