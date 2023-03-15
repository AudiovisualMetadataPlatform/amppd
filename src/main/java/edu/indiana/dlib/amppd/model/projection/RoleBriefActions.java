package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.ac.Role;

/**
 * Projection for a brief view of a role.
 * @author yingfeng
 */
@Projection(name = "briefActions", types = {Role.class}) 
public interface RoleBriefActions extends RoleBrief, AmpObjectBrief {

	public Set<ActionBrief> getActions();

}
