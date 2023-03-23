package edu.indiana.dlib.amppd.model.projection;

import org.springframework.data.rest.core.config.Projection;
import org.springframework.http.HttpMethod;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;

/**
 * Projection for a brief view of an action.
 * @author yingfeng
 */
@Projection(name = "brief", types = {Action.class}) 
public interface ActionBrief extends AmpObjectBrief {

	public String getName();
	public String getDescription();	
	public Boolean getConfigurable();	
    public ActionType getActionType();
    public TargetType getTargetType();
    public HttpMethod getHttpMethod();
    public String getUrlPattern();  

}
