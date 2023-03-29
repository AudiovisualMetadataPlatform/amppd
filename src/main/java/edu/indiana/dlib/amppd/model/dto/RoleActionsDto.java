package edu.indiana.dlib.amppd.model.dto;

import java.util.HashSet;
import java.util.Set;

import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


/**
 * Class for Role DTO with ActionDTOs.
 * @author yingfeng
 */
@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@NoArgsConstructor
@AllArgsConstructor
public class RoleActionsDto extends RoleDto {

	public Set<ActionDto> actions;
    
    public RoleActionsDto(Role role) {
    	super(role);    	
    	actions = new HashSet<ActionDto>();
    	
    	for (Action action : role.getActions()) {
    		ActionDto actionDto = new ActionDto(action);
    		actions.add(actionDto);
    	}
    }
    
}
