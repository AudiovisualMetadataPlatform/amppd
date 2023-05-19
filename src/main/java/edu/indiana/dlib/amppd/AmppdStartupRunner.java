package edu.indiana.dlib.amppd;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import edu.indiana.dlib.amppd.service.AmpUserService;
import edu.indiana.dlib.amppd.service.MgmRefreshService;
import edu.indiana.dlib.amppd.service.PermissionRefreshService;
import edu.indiana.dlib.amppd.service.impl.DataentityServiceImpl;

@Component
public class AmppdStartupRunner  implements CommandLineRunner {

	@Autowired
	private PermissionRefreshService permissionRefreshService;

	@Autowired
	private MgmRefreshService mgmRefreshService;
	
	@Autowired
	private AmpUserService ampUserService;

	@Autowired
	private DataentityServiceImpl dataentityServiceImpl;

	
    @Override
    public void run(String...args) throws Exception {	
		// initialize/refresh access control tables
		permissionRefreshService.refreshPermissionTables();

		// initialize/refresh MGM tables
		mgmRefreshService.refreshMgmTables();

		// initialize/refresh Unit table
		dataentityServiceImpl.refreshUnit();

		// bootstrap AMP admin user
    	ampUserService.bootstrapAdmin();		
    }
    
}