package edu.indiana.dlib.amppd.model.dto;

import java.util.HashSet;
import java.util.Set;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Action;
import edu.indiana.dlib.amppd.model.ac.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class for Role with Actions projection, used for role_action configuration.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleActionsDto {

    private Long id;
    private Long unitId;
    private String name;    
    private Integer level;	
	public Set<ActionDto> actions;
    
    public RoleActionsDto(Role role) {
    	id = role.getId();
    	Unit unit = role.getUnit(); 
    	unitId = unit == null ? null : unit.getId();
    	name = role.getName();
    	level = role.getLevel();   
    	actions = new HashSet<ActionDto>();
    	
    	for (Action action : role.getActions()) {
    		ActionDto actionDto = new ActionDto(action);
    		actions.add(actionDto);
    	}
    }
    
}
