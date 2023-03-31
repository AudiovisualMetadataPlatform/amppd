package edu.indiana.dlib.amppd.model.dto;

import org.springframework.http.HttpMethod;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Action.ActionType;
import edu.indiana.dlib.amppd.model.ac.Action.TargetType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Class for Action brief DTO.
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

    /**
     * Construct ActionDto from Action.
     */
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
    
    /**
     * Copy ActionDto fields to Action, except ID.
     */
    public void copyTo(Action action) {
    	action.setName(name);
    	action.setDescription(description);
    	action.setConfigurable(configurable);
    	action.setActionType(actionType);
    	action.setTargetType(targetType);
    	action.setHttpMethod(httpMethod);
    	action.setUrlPattern(urlPattern);
    }
    
}
