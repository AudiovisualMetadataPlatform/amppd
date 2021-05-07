package edu.indiana.dlib.amppd.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory;

import edu.indiana.dlib.amppd.config.GalaxyPropertyConfig;
import edu.indiana.dlib.amppd.model.galaxy.GalaxyUser;
import edu.indiana.dlib.amppd.service.GalaxyApiService;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of Galaxy service.
 * @author yingfeng
 *
 */
// TODO change commented code to use galaxyRestTemplate as autowired
//@ContextConfiguration(classes = GalaxyRestTemplateFactoryConfig.class)
@Service
@Slf4j
public class GalaxyApiServiceImpl implements GalaxyApiService {

	@Autowired
	private GalaxyPropertyConfig config;
		
	// master Galaxy user for all AMP users
	private GalaxyUser galaxyUser = null;

	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getApiKey()
	 */
	public GalaxyUser getGalaxyUser() {
		/* TODO
		 * Below is a temporary implementation, which always returns the master Galaxy user for any AMP user.
		 * Once user management and access control is impl'ed in AMP, it shall return a Galaxy user instance for the current AMP user.
		 * Also, this class could be merged into user management and access control service.
		 */
		if (galaxyUser == null) {		
			galaxyUser = new GalaxyUser(config.getUsername(), config.getPassword());
		}
		return galaxyUser;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getInstance()
	 */
	public GalaxyInstance getGalaxyInstance() {
		// if the galaxy instance has already been retrieved and stored in the current user, no need to retrieve again 
		if (galaxyUser.getInstance() != null) {
			return galaxyUser.getInstance();
		}		
		
		// otherwise create a new Galaxy instance using user's credentials and store it for the current user
		GalaxyInstance instance = null;
		try {
			// Galaxy server returns API key with Galaxy user email and password, stored as config properties;
			// note that here username is the same as Galaxy email, but might differ from Galaxy username.
			instance = GalaxyInstanceFactory.getFromCredentials(config.getBaseUrl(), user.getUsername(), user.getPassword());
		}
		catch (Exception e) {
			String msg = "Unable to acquire Galaxy instance for user " + user.getUsername() + " at " + config.getBaseUrl();
			log.error(msg);
			throw new RuntimeException(msg, e);
		}
		
		user.setInstance(instance);
		return instance;
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getApiKey()
	 */
	public String getApiKey() {
		// if the API key has already been retrieved and stored in the current user, no need to retrieve again 
		GalaxyUser user = getGalaxyUser();
		if (user.getApiKey() != null) {
			return user.getApiKey();
		}
		
		// otherwise create a new Galaxy instance using user's credentials and store it for the current user
		String key = getGalaxyInstance().getApiKey();
		user.setApiKey(key);
		return key;
	}

}
