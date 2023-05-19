package edu.indiana.dlib.amppd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.DataentityService;
import edu.indiana.dlib.amppd.service.MgmRefreshService;
import edu.indiana.dlib.amppd.service.PermissionRefreshService;

@Component
public class AmppdStartupRunner  implements CommandLineRunner {

	@Autowired
	private PermissionRefreshService permissionRefreshService;

	@Autowired
	private MgmRefreshService mgmRefreshService;
	
	@Autowired
	private DataentityService dataentityService;

	@Autowired
	private AmpUserService ampUserService;

	
    @Override
    public void run(String...args) throws Exception {	
		// initialize/refresh access control tables
		permissionRefreshService.refreshPermissionTables();

		// initialize/refresh MGM tables
		mgmRefreshService.refreshMgmTables();

		// initialize/refresh Unit table
		dataentityService.refreshUnit();

		// bootstrap AMP admin user
    	ampUserService.bootstrapAdmin();		
    }
    
}