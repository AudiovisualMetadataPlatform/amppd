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
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getApiKey()
	 */
	public GalaxyUser getCurrentUser() {
		/* TODO
		 * Below is a stub for the real implementation: for now we will return a new instance of the amppd master user as the current user;
		 * once we set up user management and access control in AMP, we shall retrieve this info from the current user session.  
		 * Also, once we implement Amppd User, this method can be moved to UserService.
		 */
		GalaxyUser user = new GalaxyUser();
		user.setUsername(config.getUsername());
		user.setPassword(config.getPassword());
		return user;		
	}
	
	/**
	 * @see edu.indiana.dlib.amppd.service.GalaxyApiService.getInstance()
	 */
	public GalaxyInstance getGalaxyInstance() {
		GalaxyUser user = getCurrentUser();
		
		// if the galaxy instance has already been retrieved and stored in the current user, no need to retrieve again 
		if (user.getInstance() != null) {
			return user.getInstance();
		}		
		
		// otherwise create a new Galaxy instance using user's credentials and store it for the current user
		GalaxyInstance instance = null;
		try {
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
		GalaxyUser user = getCurrentUser();
		if (user.getApiKey() != null) {
			return user.getApiKey();
		}
		
		// otherwise create a new Galaxy instance using user's credentials and store it for the current user
		String key = getGalaxyInstance().getApiKey();
		user.setApiKey(key);
		return key;
	}

}
