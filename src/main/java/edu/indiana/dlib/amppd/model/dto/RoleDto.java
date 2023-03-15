package edu.indiana.dlib.amppd.model.dto;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class for Role projection, used for role assignment.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {

    private Long id;
    private Long unitId;
    private String name;    
    private Integer level;	

    public RoleDto(Role role) {
    	id = role.getId();
    	Unit unit = role.getUnit(); 
    	unitId = unit == null ? null : unit.getId();
    	name = role.getName();
    	level = role.getLevel();    	
    }
    
}
