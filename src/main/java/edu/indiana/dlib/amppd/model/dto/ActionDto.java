package edu.indiana.dlib.amppd.model.dto;

import org.springframework.http.HttpMethod;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class for Action projection, used for role_action configuration.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionDto {
	private Long id;   
    private String name;   
    private String description;
    private Boolean configurable;    
    private ActionType actionType;
    private TargetType targetType;
    private HttpMethod httpMethod;
    private String urlPattern;    

    public ActionDto(Action action) {
    	id = action.getId();
    	name = action.getName();
    	description = action.getDescription();   
    	configurable = action.getConfigurable();
    	actionType = action.getActionType();
    	targetType = action.getTargetType();
    	httpMethod = action.getHttpMethod();
    	urlPattern = action.getUrlPattern();
    }
    
}
