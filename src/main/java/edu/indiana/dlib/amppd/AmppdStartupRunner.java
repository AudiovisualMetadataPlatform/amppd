package edu.indiana.dlib.amppd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.MgmRefreshService;
import edu.indiana.dlib.amppd.service.PermissionRefreshService;

@Component
public class AmppdStartupRunner  implements CommandLineRunner {

	@Autowired
	private AmpUserService ampUserService;

	@Autowired
	private PermissionRefreshService permissionRefreshService;

	@Autowired
	private MgmRefreshService mgmRefreshService;
	
    @Override
    public void run(String...args) throws Exception {	
    	// bootstrap AMP admin user
    	ampUserService.bootstrapAdmin();
    	
		// initialize/refresh access control tables
		permissionRefreshService.refreshPermissionTables();

		// initialize/refresh MGM tables
		mgmRefreshService.refreshMgmTables();
    }
    
}