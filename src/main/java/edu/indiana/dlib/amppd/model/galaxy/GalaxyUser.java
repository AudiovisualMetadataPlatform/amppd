package edu.indiana.dlib.amppd.model.galaxy;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper class to include the basic info of a Galaxy user.
 * @author yingfeng
 *
 */
@Data
@NoArgsConstructor
public class GalaxyUser {

	private String username;
	private String password;
	private String apiKey;
	
}
