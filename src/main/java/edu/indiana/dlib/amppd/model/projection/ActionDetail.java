package edu.indiana.dlib.amppd.model.projection;

import java.util.Set;

import org.springframework.data.rest.core.config.Projection;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;


/**
 * Projection for a detailed view of an action.
 * @author yingfeng
 */
@Projection(name = "detail", types = {Action.class}) 
public interface ActionDetail extends ActionBrief, AmpObjectDetail {

	public Set<Role> getRoles();

}