package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.AmpUser;
import edu.indiana.dlib.amppd.model.AmpUser.Status;


/**
 * Projection for a brief view of a user.
 * @author yingfeng
 */
@Projection(name = "brief", types = {AmpUser.class}) 
public interface AmpUserBrief extends AmpObjectBrief {

	public String getUsername();
	public String getEmail();	
//	public String getPassword();
	public String getFirstName();
	public String getLastName();
	public Status getStatus();

}
