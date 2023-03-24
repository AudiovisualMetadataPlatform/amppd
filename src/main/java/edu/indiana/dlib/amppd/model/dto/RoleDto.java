package edu.indiana.dlib.amppd.model.dto;

import edu.indiana.dlib.amppd.model.Unit;
import edu.indiana.dlib.amppd.model.ac.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Class for Role brief DTO.
 * @author yingfeng
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {

    private Long id;
    private Long unitId;
    private String name;    
    private String description;
    private Integer level;	

    /**
     * Construct RoleDto from Role.
     */
    public RoleDto(Role role) {
    	id = role.getId();
    	Unit unit = role.getUnit(); 
    	unitId = unit == null ? null : unit.getId();
    	name = role.getName();
    	level = role.getLevel();   
    }
    
    /**
     * Copy RoleDto fields to Role, except IDs.
     */
    public void copyTo(Role role) {
    	role.setName(name);
    	role.setDescription(description);
    	role.setLevel(level);
    }
    
}
