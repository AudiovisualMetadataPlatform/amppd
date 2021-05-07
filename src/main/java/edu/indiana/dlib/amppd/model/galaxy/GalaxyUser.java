package edu.indiana.dlib.amppd.model.galaxy;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;

import lombok.Data;
import lombok.NoArgsConstructor;

// TODO This class can be refactored to be included in AMP User class.

/**
 * Wrapper class to include the basic info of a Galaxy user.
 * @author yingfeng
 *
 */
@Data
@NoArgsConstructor
public class GalaxyUser {

	private String userId;
	private String username;
	private String password;
	private String apiKey;
	private GalaxyInstance instance;
	
	public GalaxyUser(String userId, String username, String password) {
		this.userId = userId;
		this.username = username;
		this.password = password;		
	}
	
}
