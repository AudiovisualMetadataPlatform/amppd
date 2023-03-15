package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.AmpUser;


/**
 * Projection for a detailed view of a user.
 * @author yingfeng
 */
@Projection(name = "detail", types = {AmpUser.class}) 
public interface AmpUserDetail extends AmpUserBrief, AmpObjectDetail {

    public Set<RoleAssignmentBrief> getRoleAssignements();	

}